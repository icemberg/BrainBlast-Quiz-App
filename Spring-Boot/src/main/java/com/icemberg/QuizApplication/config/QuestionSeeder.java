package com.icemberg.QuizApplication.config;

import com.icemberg.QuizApplication.repository.QuestionRepository;
import com.icemberg.QuizApplication.entity.Question;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.HtmlUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class QuestionSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(QuestionSeeder.class);
    private final QuestionRepository questionDao;
    private final RestTemplate restTemplate;

    public QuestionSeeder(QuestionRepository questionDao) {
        this.questionDao = questionDao;
        this.restTemplate = new RestTemplate();
    }

    @org.springframework.beans.factory.annotation.Value("${app.seeder.enabled:true}")
    private boolean isSeederEnabled;

    // Mapping Internal Category -> {Total Target Count, API Category ID}
    private static final Map<String, CategoryConfig> CATEGORY_CONFIGS = new LinkedHashMap<>();

    static {
        // Targets based on Landing Page requirements
        CATEGORY_CONFIGS.put("General", new CategoryConfig(500, 9));
        CATEGORY_CONFIGS.put("Science", new CategoryConfig(85, 17));
        CATEGORY_CONFIGS.put("History", new CategoryConfig(200, 23));
        CATEGORY_CONFIGS.put("Sports", new CategoryConfig(150, 21));
        CATEGORY_CONFIGS.put("Arts", new CategoryConfig(64, 25));
        CATEGORY_CONFIGS.put("Technology", new CategoryConfig(120, 18));
    }

    private static class CategoryConfig {
        int totalTarget;
        int apiCategoryId;

        public CategoryConfig(int totalTarget, int apiCategoryId) {
            this.totalTarget = totalTarget;
            this.apiCategoryId = apiCategoryId;
        }
    }

    @Override
    public void run(String... args) {
        if (!isSeederEnabled) {
            logger.info("🚫 Question Seeder is disabled via property. Skipping seeding.");
            return;
        }

        logger.info("📢 Starting Rigid ETL Question Seeder...");

        // 0. Manual Seeding (Priority)
        seedManualQuestions();

        for (Map.Entry<String, CategoryConfig> entry : CATEGORY_CONFIGS.entrySet()) {
            String category = entry.getKey();
            CategoryConfig config = entry.getValue();
            
            processCategory(category, config);
        }

        logger.info("✅ ETL Seeding Process Completed.");
    }

    private void processCategory(String category, CategoryConfig config) {
        logger.info("🔍 Verifying Category: {}", category);

        // 1. Calculate Targets (Equal Distribution)
        int targetPerDifficulty = (int) Math.ceil(config.totalTarget / 3.0);
        
        // 2. Pre-Verification & Fetching
        boolean needsRetry = false;
        
        needsRetry |= fetchAndLoadIfNeeded(category, config.apiCategoryId, "Easy", targetPerDifficulty);
        needsRetry |= fetchAndLoadIfNeeded(category, config.apiCategoryId, "Medium", targetPerDifficulty);
        needsRetry |= fetchAndLoadIfNeeded(category, config.apiCategoryId, "Hard", targetPerDifficulty);

        // 3. Post-Verification
        if (needsRetry || !verifyCategoryCounts(category, targetPerDifficulty)) {
            logger.warn("⚠️ Validation failed for {}. Initiating Retry Protocol with Rate Limits...", category);
            executeRetryProtocol(category, config.apiCategoryId, targetPerDifficulty);
        } else {
            logger.info("✨ Category {} is fully populated and verified.", category);
        }
    }

    private boolean fetchAndLoadIfNeeded(String category, int apiId, String difficulty, int target) {
        long current = questionDao.countByCategoryAndDifficultylevel(category, difficulty);
        int missing = target - (int) current;

        if (missing <= 0) {
            logger.info("   ✔️ {} {} satisfied (Found: {}, Target: {})", category, difficulty, current, target);
            return false; // No fetch needed
        }

        logger.info("   📥 Fetching {} missing {} questions for {}", missing, difficulty, category);
        fetchFromApi(category, apiId, difficulty, missing);
        
        // Return true if we attempted a fetch, treating it as "change occurred, valid verify needed"
        return true; 
    }

    private void seedManualQuestions() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            java.io.InputStream inputStream = getClass().getResourceAsStream("/manual_questions.json");
            
            if (inputStream == null) {
                logger.warn("⚠️ manual_questions.json not found. Skipping manual seeding.");
                return;
            }

            ApiResponse manualData = mapper.readValue(inputStream, ApiResponse.class);
            if (manualData != null && manualData.results != null) {
                logger.info("📂 Creating {} manual questions...", manualData.results.size());
                
                // Group by Category to use existing logic if possible, or just loop
                // Since JSON has 'Arts' etc, we need to map or just save.
                // The JSON has "Arts" but our internal category is "Arts".
                // Note: User provided JSON has "Art" and "Arts". 
                // We should map "Art" to "Arts" just in case.

                for (ApiQuestion apiQ : manualData.results) {
                    // Normalize Category
                    String cat = apiQ.category;
                    if (cat.equals("Art")) cat = "Arts"; 
                    if (cat.equals("Science: Computers")) cat = "Computer";
                    if (cat.equals("Science: Mathematics")) cat = "Math";
                    
                    // Normalize Difficulty
                    String diff = apiQ.difficulty; // "easy", "medium", "hard"
                    if (diff != null) {
                        diff = diff.substring(0, 1).toUpperCase() + diff.substring(1).toLowerCase();
                    }

                    saveSingleManualQuestion(apiQ, cat, diff);
                }
            }
        } catch (Exception e) {
            logger.error("❌ Failed to seed manual questions: {}", e.getMessage());
        }
    }

    private void saveSingleManualQuestion(ApiQuestion apiQ, String category, String difficulty) {
        String title = HtmlUtils.htmlUnescape(apiQ.question);
        if (!questionDao.existsByQuestionTitleAndCategory(title, category)) {
            Question q = new Question();
            q.setCategory(category);
            q.setDifficultylevel(difficulty);
            q.setQuestionTitle(title);
            
            List<String> options = new ArrayList<>(apiQ.incorrectAnswers);
            options.add(apiQ.correctAnswer);
            Collections.shuffle(options);
            
            q.setOption1(HtmlUtils.htmlUnescape(options.get(0)));
            q.setOption2(HtmlUtils.htmlUnescape(options.get(1)));
            q.setOption3(HtmlUtils.htmlUnescape(options.get(2)));
            q.setOption4(HtmlUtils.htmlUnescape(options.get(3)));
            q.setRightAnswer(HtmlUtils.htmlUnescape(apiQ.correctAnswer));
            
            questionDao.save(q);
        }
    }

    private void fetchFromApi(String internalCategory, int apiId, String difficulty, int countToFetch) {
        int loaded = 0;
        int batchSize = 50; 
        int errors = 0;

        String apiDifficulty = difficulty.toLowerCase(); 

        while (loaded < countToFetch && errors < 5) { 
            int amount = Math.min(batchSize, countToFetch - loaded);
            
            String urlOpenTdb = String.format("https://opentdb.com/api.php?amount=%d&category=%d&difficulty=%s&type=multiple", 
                amount, apiId, apiDifficulty);
            String urlOtriviata = String.format("https://www.otriviata.com/api.php?amount=%d&category=%d&difficulty=%s&type=multiple", 
                amount, apiId, apiDifficulty);
            
            try {
                // Rate Limiting - INCREASED to 5s to avoid 429
                TimeUnit.MILLISECONDS.sleep(5000); 

                logger.info("      🚀 Launching simultaneous requests to OpenTDB and Otriviata for {} questions...", amount);

                java.util.concurrent.CompletableFuture<ApiResponse> futureOpenTdb = java.util.concurrent.CompletableFuture.supplyAsync(() -> {
                    try {
                        return restTemplate.getForObject(urlOpenTdb, ApiResponse.class);
                    } catch (Exception e) {
                        logger.error("      OpenTDB Failure: {}", e.getMessage());
                        return null;
                    }
                });

                java.util.concurrent.CompletableFuture<ApiResponse> futureOtriviata = java.util.concurrent.CompletableFuture.supplyAsync(() -> {
                    try {
                        return restTemplate.getForObject(urlOtriviata, ApiResponse.class);
                    } catch (Exception e) {
                        logger.error("      Otriviata Failure: {}", e.getMessage());
                        return null;
                    }
                });

                java.util.concurrent.CompletableFuture.allOf(futureOpenTdb, futureOtriviata).join();

                ApiResponse res1 = futureOpenTdb.get();
                ApiResponse res2 = futureOtriviata.get();

                List<ApiQuestion> combinedResults = new ArrayList<>();
                if (res1 != null && res1.results != null) combinedResults.addAll(res1.results);
                if (res2 != null && res2.results != null) combinedResults.addAll(res2.results);

                if (!combinedResults.isEmpty()) {
                    List<Question> savedBatch = saveBatch(combinedResults, internalCategory, difficulty);
                    loaded += savedBatch.size();
                    
                    if (savedBatch.isEmpty()) {
                        logger.info("      (Batch yielded 0 new unique questions. Proceeding...)");
                        errors++; 
                    } else {
                        errors = 0; 
                    }
                } else {
                    logger.warn("      BOTH APIs returned empty/null/error. Reducing batch size...");
                    errors++;
                    batchSize = Math.max(10, batchSize / 2); // Reduce questions asked
                    TimeUnit.SECONDS.sleep(5);
                }

            } catch (Exception e) {
                logger.error("      Parallel Fetch Error: {}", e.getMessage());
                errors++;
            }
        }
    }

    private List<Question> saveBatch(List<ApiQuestion> results, String category, String difficulty) {
        List<Question> toSave = new ArrayList<>();
        
        for (ApiQuestion apiQ : results) {
            String title = HtmlUtils.htmlUnescape(apiQ.question);
            
            if (!questionDao.existsByQuestionTitleAndCategory(title, category)) {
                Question q = new Question();
                q.setCategory(category);
                q.setDifficultylevel(difficulty); // Stored as Easy/Medium/Hard (Capitalized)
                q.setQuestionTitle(title);
                
                // Options Logic
                List<String> options = new ArrayList<>(apiQ.incorrectAnswers);
                options.add(apiQ.correctAnswer);
                Collections.shuffle(options);
                
                q.setOption1(HtmlUtils.htmlUnescape(options.get(0)));
                q.setOption2(HtmlUtils.htmlUnescape(options.get(1)));
                q.setOption3(HtmlUtils.htmlUnescape(options.get(2)));
                q.setOption4(HtmlUtils.htmlUnescape(options.get(3)));
                q.setRightAnswer(HtmlUtils.htmlUnescape(apiQ.correctAnswer));
                
                toSave.add(q);
            }
        }
        
        if (!toSave.isEmpty()) {
            questionDao.saveAll(toSave);
            logger.info("      💾 Saved {} new questions.", toSave.size());
        }
        return toSave;
    }

    private boolean verifyCategoryCounts(String category, int targetPerDiff) {
        long easy = questionDao.countByCategoryAndDifficultylevel(category, "Easy");
        long medium = questionDao.countByCategoryAndDifficultylevel(category, "Medium");
        long hard = questionDao.countByCategoryAndDifficultylevel(category, "Hard");
        
        boolean valid = easy >= targetPerDiff && medium >= targetPerDiff && hard >= targetPerDiff;
        
        if (!valid) {
            logger.warn("   ❌ Verification Mismatch for {}: Easy={}/{}, Medium={}/{}, Hard={}/{}", 
                category, easy, targetPerDiff, medium, targetPerDiff, hard, targetPerDiff);
        }
        return valid;
    }

    private void executeRetryProtocol(String category, int apiId, int target) {
        try {
            logger.info("   ⏳ Waiting 60s before RETRY protocol for {}...", category);
            TimeUnit.SECONDS.sleep(60); 

            // Retry Easy
            if (questionDao.countByCategoryAndDifficultylevel(category, "Easy") < target) {
                logger.info("   🔄 Retrying Easy...");
                fetchFromApi(category, apiId, "Easy", target - (int)questionDao.countByCategoryAndDifficultylevel(category, "Easy"));
                TimeUnit.SECONDS.sleep(60);
            }

            // Retry Medium
            if (questionDao.countByCategoryAndDifficultylevel(category, "Medium") < target) {
                 logger.info("   🔄 Retrying Medium...");
                 fetchFromApi(category, apiId, "Medium", target - (int)questionDao.countByCategoryAndDifficultylevel(category, "Medium"));
                 TimeUnit.SECONDS.sleep(60);
            }

            // Retry Hard
            if (questionDao.countByCategoryAndDifficultylevel(category, "Hard") < target) {
                 logger.info("   🔄 Retrying Hard...");
                 fetchFromApi(category, apiId, "Hard", target - (int)questionDao.countByCategoryAndDifficultylevel(category, "Hard"));
            }
            
            logger.info("   ℹ️ Retry Protocol Finished for {}.", category);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Retry Interrupted", e);
        }
    }

    // --- DTOs ---

    public static class ApiResponse {
        @JsonProperty("response_code")
        public int responseCode;
        public List<ApiQuestion> results;
    }

    public static class ApiQuestion {
        public String category;
        public String type;
        public String difficulty;
        public String question;
        
        @JsonProperty("correct_answer")
        public String correctAnswer;
        
        @JsonProperty("incorrect_answers")
        public List<String> incorrectAnswers;
    }
}
