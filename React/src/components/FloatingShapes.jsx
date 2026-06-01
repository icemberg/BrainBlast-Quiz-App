import React from 'react';
import { motion } from 'framer-motion';

const FloatingShapes = () => (
    <div className="absolute inset-0 overflow-hidden pointer-events-none z-0">
        <motion.div
            animate={{
                y: [0, -50, 0],
                rotate: [0, 180, 0],
                scale: [1, 1.2, 1],
            }}
            transition={{ duration: 20, repeat: Infinity, ease: "linear" }}
            className="absolute top-20 left-10 w-72 h-72 bg-purple-600/20 rounded-full blur-3xl"
        />
        <motion.div
            animate={{
                y: [0, 50, 0],
                x: [0, 30, 0],
                scale: [1, 1.1, 1],
            }}
            transition={{ duration: 15, repeat: Infinity, ease: "linear" }}
            className="absolute bottom-20 right-10 w-96 h-96 bg-pink-600/20 rounded-full blur-3xl"
        />
        <div className="absolute inset-0 bg-gradient-to-b from-transparent to-black/80" />
    </div>
);

export default FloatingShapes;
