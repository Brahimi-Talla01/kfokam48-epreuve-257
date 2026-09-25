import type { Metadata } from "next";
import type { ReactNode } from "react";
import Link from "next/link";
import { API_BASE_URL } from "@/lib/api";
import "./globals.css";

export const metadata: Metadata = {
  title: "KFOKAM48 : Présence et relecture par les pairs",
  description:
    "Ouverture de session, code de présence, dépôt d'exercices et relecture par les pairs.",
};

export default function RootLayout({ children }: { children: ReactNode }) {
  return (
    <html lang="fr">
      <body>
        <header className="site-header">
          <div className="container site-header__inner">
            <Link href="/" className="site-title">
              <span className="site-title__badge" aria-hidden="true">
                K48
              </span>
              KFOKAM48
            </Link>
            <nav className="nav" aria-label="Navigation principale">
              <Link href="/formateur">Formateur</Link>
              <Link href="/etudiant">Étudiant</Link>
              <Link href="/relecteur">Relecteur</Link>
            </nav>
          </div>
        </header>

        <main className="container main">{children}</main>

        <footer className="site-footer">
          <div className="container">
            API interrogée : <code>{API_BASE_URL}</code>
          </div>
        </footer>
      </body>
    </html>
  );
}
