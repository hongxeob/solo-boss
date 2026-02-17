import type { Config } from "tailwindcss";

const config: Config = {
  content: [
    "./pages/**/*.{js,ts,jsx,tsx,mdx}",
    "./components/**/*.{js,ts,jsx,tsx,mdx}",
    "./app/**/*.{js,ts,jsx,tsx,mdx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: "#643A71",
        secondary: "#FEC0CE",
        background: "#0A0A0A", // 다크 모드 배경
        surface: "#1A1A1A",    // 카드 등 표면 색상
      },
    },
  },
  plugins: [],
};
export default config;
