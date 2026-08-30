import { useEffect, useMemo, useState } from "react";
import { educationService } from "../services/api";

const defaultCategory = "all";

const Education = () => {
  const [resources, setResources] = useState([]);
  const [selectedCategory, setSelectedCategory] = useState(defaultCategory);
  const [selectedResource, setSelectedResource] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const loadResources = async (category = selectedCategory) => {
    setLoading(true);
    setError("");

    try {
      const params =
        category && category !== defaultCategory ? { category } : {};
      const { data } = await educationService.getResources(params);
      setResources(data || []);
      setSelectedResource((current) => {
        if (data?.length && (!current || current.category !== category)) {
          return data[0];
        }
        return current;
      });
    } catch (err) {
      setError(
        err.response?.data?.message ||
          err.message ||
          "Unable to load education resources right now.",
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadResources(selectedCategory);
  }, [selectedCategory]);

  const categories = useMemo(() => {
    const unique = new Set(
      resources.map((resource) => resource.category).filter(Boolean),
    );
    return [defaultCategory, ...Array.from(unique)];
  }, [resources]);

  const openResource = async (resource) => {
    try {
      const { data } = await educationService.getResource(resource.id);
      setSelectedResource(data || resource);
    } catch (err) {
      setSelectedResource(resource);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-emerald-50 px-4 py-6">
        <div className="mx-auto max-w-7xl space-y-4">
          <div className="animate-pulse rounded-3xl bg-white p-6 shadow-sm ring-1 ring-slate-100">
            <div className="h-6 w-40 rounded bg-slate-100" />
            <div className="mt-4 h-10 w-72 rounded bg-slate-100" />
          </div>
          <div className="grid gap-4 md:grid-cols-3">
            {[0, 1, 2].map((item) => (
              <div
                key={item}
                className="animate-pulse rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-100"
              >
                <div className="h-4 w-20 rounded bg-slate-100" />
                <div className="mt-4 h-8 w-28 rounded bg-slate-100" />
              </div>
            ))}
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-emerald-50 px-4 py-6">
        <div className="mx-auto max-w-3xl rounded-2xl border border-red-200 bg-white p-6 text-center shadow-sm">
          <p className="text-lg font-semibold text-slate-900">
            Unable to load learning resources
          </p>
          <p className="mt-2 text-sm text-slate-600">{error}</p>
          <button
            type="button"
            onClick={() => loadResources(selectedCategory)}
            className="mt-5 rounded-xl bg-emerald-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-emerald-700"
          >
            Try again
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-emerald-50 px-4 py-6">
      <div className="mx-auto max-w-7xl space-y-6">
        <section className="rounded-3xl bg-gradient-to-r from-amber-500 to-orange-500 p-6 text-white shadow-lg shadow-amber-100">
          <p className="text-sm font-medium uppercase tracking-[0.2em] text-amber-100">
            Education
          </p>
          <h1 className="mt-2 text-3xl font-bold">Farmer learning hub</h1>
          <p className="mt-2 text-sm text-amber-50">
            Access practical guidance for soil, irrigation, pest control,
            weather, and market practices.
          </p>
        </section>

        <section className="rounded-3xl bg-white p-5 shadow-sm ring-1 ring-slate-100">
          <div className="flex flex-wrap gap-2">
            {categories.map((category) => (
              <button
                key={category}
                type="button"
                onClick={() => setSelectedCategory(category)}
                className={`rounded-full px-3 py-2 text-sm font-medium transition ${
                  selectedCategory === category
                    ? "bg-emerald-600 text-white"
                    : "bg-slate-100 text-slate-700 hover:bg-slate-200"
                }`}
              >
                {category === defaultCategory ? "All topics" : category}
              </button>
            ))}
          </div>
        </section>

        {resources.length === 0 ? (
          <section className="rounded-3xl border border-dashed border-slate-200 bg-white p-8 text-center shadow-sm">
            <p className="text-xl font-semibold text-slate-900">
              No educational resources are available yet
            </p>
            <p className="mt-2 text-sm text-slate-600">
              New learning material appears here when it is added to the backend
              catalog.
            </p>
          </section>
        ) : (
          <section className="grid gap-6 xl:grid-cols-[0.9fr_1.1fr]">
            <div className="space-y-4">
              {resources.map((resource) => (
                <button
                  key={resource.id}
                  type="button"
                  onClick={() => openResource(resource)}
                  className={`w-full rounded-3xl border p-4 text-left shadow-sm transition ${
                    selectedResource?.id === resource.id
                      ? "border-emerald-300 bg-emerald-50"
                      : "border-slate-200 bg-white hover:bg-slate-50"
                  }`}
                >
                  <div className="flex items-start justify-between gap-3">
                    <div>
                      <p className="text-xs uppercase tracking-wide text-slate-500">
                        {resource.category || "General"}
                      </p>
                      <h2 className="mt-2 text-lg font-semibold text-slate-900">
                        {resource.title}
                      </h2>
                    </div>
                    <span className="rounded-full bg-amber-100 px-2 py-1 text-[10px] font-semibold uppercase tracking-wide text-amber-700">
                      {resource.language || "en"}
                    </span>
                  </div>
                  <p className="mt-3 line-clamp-3 text-sm text-slate-600">
                    {resource.content}
                  </p>
                </button>
              ))}
            </div>

            <div className="rounded-3xl bg-white p-5 shadow-sm ring-1 ring-slate-100">
              {selectedResource ? (
                <>
                  <p className="text-xs uppercase tracking-[0.2em] text-slate-500">
                    {selectedResource.category || "General"}
                  </p>
                  <h2 className="mt-3 text-2xl font-bold text-slate-900">
                    {selectedResource.title}
                  </h2>
                  <p className="mt-3 text-sm text-slate-500">
                    Language: {selectedResource.language || "en"}
                  </p>
                  <div className="mt-5 rounded-2xl bg-amber-50 p-4">
                    <p className="text-sm leading-7 text-slate-700 whitespace-pre-line">
                      {selectedResource.content}
                    </p>
                  </div>
                  {selectedResource.externalUrl && (
                    <a
                      href={selectedResource.externalUrl}
                      target="_blank"
                      rel="noreferrer"
                      className="mt-5 inline-flex rounded-xl bg-emerald-600 px-4 py-2 text-sm font-medium text-white hover:bg-emerald-700"
                    >
                      Open resource
                    </a>
                  )}
                </>
              ) : (
                <div className="flex h-full min-h-[220px] items-center justify-center rounded-2xl border border-dashed border-slate-200 bg-slate-50 p-6 text-center">
                  <p className="text-sm text-slate-500">
                    Choose a learning topic to read the full guidance.
                  </p>
                </div>
              )}
            </div>
          </section>
        )}
      </div>
    </div>
  );
};

export default Education;
