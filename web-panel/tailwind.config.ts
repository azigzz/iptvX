import type { Config } from "tailwindcss";

const config: Config = {
  content: ["./src/**/*.{ts,tsx}"],
  theme: {
    extend: {
      colors: {
        ink: "#0b0f14",
        panel: "#121922",
        panelSoft: "#182230",
        line: "#2b394a",
        signal: "#27d6a5",
        warn: "#ffb020"
      }
    }
  },
  plugins: []
};

export default config;
