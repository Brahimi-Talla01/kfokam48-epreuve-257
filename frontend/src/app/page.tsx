import Link from "next/link";
import { API_BASE_URL } from "@/lib/api";

const ECRANS = [
  {
    href: "/formateur",
    titre: "Formateur",
    description:
      "Ouvrir une session, afficher le code de présence, clôturer, relever une présence manuellement et consulter le tableau.",
  },
  {
    href: "/etudiant",
    titre: "Étudiant",
    description:
      "Choisir son nom, marquer sa présence avec le code, déposer puis remplacer le lien de son exercice et lire ses notes.",
  },
  {
    href: "/relecteur",
    titre: "Relecteur",
    description:
      "Consulter les relectures qui lui sont affectées et rendre une note entière 0–20 avec un commentaire.",
  },
] as const;

export default function Accueil() {
  return (
    <>
      <h1>Bienvenue</h1>
      <p className="hint">
        Application de présence et de relecture par les pairs. Choisissez votre écran :
      </p>

      <div className="grille">
        {ECRANS.map((ecran) => (
          <Link key={ecran.href} href={ecran.href} className="carte-lien">
            <div className="carte-lien__titre">{ecran.titre}</div>
            <p className="carte-lien__desc">{ecran.description}</p>
          </Link>
        ))}
      </div>

      <section className="card">
        <h2>Backend</h2>
        <p className="hint">
          Cette application appelle l&apos;API sur la variable d&apos;environnement{" "}
          <code>NEXT_PUBLIC_API_URL</code>, actuellement : <code>{API_BASE_URL}</code>. Si le
          serveur n&apos;est pas démarré, les écrans afficheront un message d&apos;erreur en
          français.
        </p>
      </section>
    </>
  );
}
