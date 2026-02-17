import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "Solo Boss | Freelancer Care",
  description: "Professional freelance business management",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ko">
      <body className="bg-background text-slate-100 min-h-screen antialiased">
        {children}
      </body>
    </html>
  );
}
