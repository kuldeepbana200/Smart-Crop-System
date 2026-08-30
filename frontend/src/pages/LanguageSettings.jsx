import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import api from "../services/api";

const languages = [
  { code: "en", name: "English" },
  { code: "hi", name: "हिन्दी" },
  { code: "or", name: "ଓଡ଼ିଆ" },
  { code: "mr", name: "मराठी" },
];

const LanguageSettings = () => {
  const { i18n } = useTranslation();
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [selected, setSelected] = useState(i18n.language || "en");

  useEffect(() => {
    setSelected(i18n.language || "en");
  }, [i18n.language]);

  const changeLanguage = async (languageCode) => {
    setSelected(languageCode);
    setError("");
    setSaving(true);

    try {
      await i18n.changeLanguage(languageCode);
      localStorage.setItem("language", languageCode);

      const token = localStorage.getItem("token");
      if (token) {
        await api.put("/auth/language", languageCode, {
          headers: {
            "Content-Type": "text/plain",
            Authorization: `Bearer ${token}`,
          },
        });
      }
    } catch (err) {
      setError(
        err.response?.data?.message ||
          err.message ||
          "Could not update the selected language.",
      );
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="rounded-3xl bg-white p-5 shadow-sm ring-1 ring-slate-100">
      <h2 className="text-xl font-bold text-slate-900">Language and region</h2>
      <p className="mt-2 text-sm text-slate-600">
        Change the app language for the current user session and save it to the backend profile.
      </p>

      <div className="mt-5 grid gap-3 md:grid-cols-2">
        {languages.map((language) => (
          <button
            key={language.code}
            type="button"
            onClick={() => changeLanguage(language.code)}
            className={`rounded-2xl border p-4 text-left transition ${
              selected === language.code
                ? "border-emerald-300 bg-emerald-50"
                : "border-slate-200 bg-slate-50 hover:bg-slate-100"
            }`}
          >
            <div className="flex items-center justify-between gap-3">
              <div>
                <p className="text-base font-semibold text-slate-900">{language.name}</p>
                <p className="text-xs uppercase tracking-wide text-slate-500">{language.code}</p>
              </div>
              {selected === language.code && (
                <span className="rounded-full bg-emerald-600 px-2 py-1 text-[10px] font-semibold uppercase tracking-wide text-white">
                  Active
                </span>
              )}
            </div>
          </button>
        ))}
      </div>

      {saving && <p className="mt-4 text-sm text-emerald-700">Saving language preference...</p>}
      {error && <p className="mt-4 text-sm text-red-700">{error}</p>}
    </div>
  );
};

export default LanguageSettings;
