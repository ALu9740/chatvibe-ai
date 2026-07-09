/** @type {import('tailwindcss').Config} */
// Amber Lounge 琥珀私语主题（支持白天/黑夜切换）
export default {
  content: ['./index.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        espresso: 'rgb(var(--cv-espresso) / <alpha-value>)',
        cocoa: 'rgb(var(--cv-cocoa) / <alpha-value>)',
        mocha: 'rgb(var(--cv-mocha) / <alpha-value>)',
        amber: {
          400: 'rgb(var(--cv-amber-400) / <alpha-value>)',
          500: 'rgb(var(--cv-amber-500) / <alpha-value>)',
          600: 'rgb(var(--cv-amber-600) / <alpha-value>)',
        },
        cream: 'rgb(var(--cv-cream) / <alpha-value>)',
        terracotta: 'rgb(var(--cv-terracotta) / <alpha-value>)',
        moss: 'rgb(var(--cv-moss) / <alpha-value>)',
      },
      fontFamily: {
        display: ['Fraunces', 'Noto Serif SC', 'serif'],
        sans: ['Geist', 'Noto Sans SC', 'sans-serif'],
      },
      boxShadow: {
        glow: '0 0 32px -8px rgba(232,163,61,0.45)',
      },
    },
  },
  plugins: [],
}
