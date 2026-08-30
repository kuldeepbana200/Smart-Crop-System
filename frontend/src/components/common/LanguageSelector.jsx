import { useState, useEffect, useRef } from "react";
import { useTranslation } from "react-i18next";
import api from "../../services/api";

const languages = [
  { code: "en", name: "English" },
  { code: "hi", name: "हिन्दी" },
  { code: "or", name: "ଓଡ଼ିଆ" },
  { code: "mr", name: "मराठी" },
];

const LanguageSelector = () => {
  const { i18n } = useTranslation();
  const [isOpen, setIsOpen] = useState(false);
  const dropdownRef = useRef(null);

  const currentLanguage =
    languages.find((lang) => lang.code === i18n.language) || languages[0];

  const changeLanguage = async (languageCode) => {
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

      setIsOpen(false);
    } catch (error) {
      console.error("Failed to change language:", error);
    }
  };

  // Close dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setIsOpen(false);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);

    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, []);

  return (
    <div className="relative" ref={dropdownRef}>
      <button
        type="button"
        onClick={() => setIsOpen((prev) => !prev)}
        className="flex items-center gap-2 rounded-lg border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 shadow-sm hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-green-500"
        aria-haspopup="listbox"
        aria-expanded={isOpen}
      >
        <span>{currentLanguage.code.toUpperCase()}</span>

        <svg
          className={`h-4 w-4 transition-transform ${
            isOpen ? "rotate-180" : ""
          }`}
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M19 9l-7 7-7-7"
          />
        </svg>
      </button>

      {isOpen && (
        <div
          className="absolute right-0 z-50 mt-2 w-52 overflow-hidden rounded-lg border border-gray-200 bg-white shadow-lg"
          role="listbox"
        >
          {languages.map((language) => {
            const isSelected = language.code === currentLanguage.code;

            return (
              <button
                key={language.code}
                type="button"
                onClick={() => changeLanguage(language.code)}
                className={`block w-full px-4 py-3 text-left text-sm transition ${
                  isSelected
                    ? "bg-green-50 font-medium text-green-700"
                    : "text-gray-700 hover:bg-gray-50"
                }`}
                role="option"
                aria-selected={isSelected}
              >
                {language.name}
              </button>
            );
          })}
        </div>
      )}
    </div>
  );
};

export default LanguageSelector;
