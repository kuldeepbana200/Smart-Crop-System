import { useEffect, useMemo, useState } from "react";
import { useAuth } from "../context/AuthContext";
import { educationService } from "../services/api";

const uiText = {
  en: {
    title: "Education",
    headline: "Farmer learning hub",
    subtitle:
      "Personalized learning topics and trusted video guidance for your crops and field conditions.",
    loading: "Loading learning topics...",
    errorTitle: "Unable to load learning topics",
    retry: "Try again",
    emptyTitle: "No learning topics are available right now",
    emptyDescription:
      "Your personalized education topics will appear here once the AI recommendations are ready.",
    watchVideo: "Watch Video",
    channel: "Channel",
    noVideos: "No videos available for this topic yet.",
    topicLabel: "Topic",
  },
  hi: {
    title: "शिक्षा",
    headline: "किसान सीखने का केंद्र",
    subtitle:
      "आपकी फसल और खेत की परिस्थितियों के लिए व्यक्तिगत शिक्षा विषय और भरोसेमंद वीडियो मार्गदर्शन।",
    loading: "शिक्षा विषय लोड हो रहे हैं...",
    errorTitle: "शिक्षा विषय लोड नहीं हो पाए",
    retry: "फिर से प्रयास करें",
    emptyTitle: "अभी कोई शिक्षा विषय उपलब्ध नहीं है",
    emptyDescription:
      "आपके लिए व्यक्तिगत शिक्षा विषय तैयार होने के बाद यहां दिखाई देंगे।",
    watchVideo: "वीडियो देखें",
    channel: "चैनल",
    noVideos: "इस विषय के लिए अभी कोई वीडियो उपलब्ध नहीं है।",
    topicLabel: "विषय",
  },
  mr: {
    title: "शिक्षण",
    headline: "शेतकरी शिक्षण केंद्र",
    subtitle:
      "तुमच्या पिकांवर आणि शेताच्या परिस्थितींवर आधारित वैयक्तिक शिक्षण विषय आणि विश्वासार्ह व्हिडिओ मार्गदर्शन।",
    loading: "शिक्षण विषय लोड होत आहेत...",
    errorTitle: "शिक्षण विषय लोड करण्यात अयशस्वी",
    retry: "पुन्हा प्रयत्न करा",
    emptyTitle: "सध्या कोणतेही शिक्षण विषय उपलब्ध नाहीत",
    emptyDescription:
      "तुमच्यासाठी वैयक्तिक शिक्षण विषय तयार झाल्यानंतर ते येथे दिसतील.",
    watchVideo: "व्हिडिओ पहा",
    channel: "चॅनेल",
    noVideos: "या विषयासाठी सध्या कोणतेही व्हिडिओ उपलब्ध नाहीत.",
    topicLabel: "विषय",
  },
  or: {
    title: "ଶିକ୍ଷା",
    headline: "କୃଷକ ଶିକ୍ଷା କେନ୍ଦ୍ର",
    subtitle:
      "ଆପଣଙ୍କ ଫସଲ ଓ କ୍ଷେତ୍ର ଅବସ୍ଥାନାନୁଁ ଆଧାରିତ ବ୍ୟକ୍ତିଗତ ଶିକ୍ଷା ବିଷୟ ଓ ଭରସା ଯୋଗ୍ୟ ଭିଡିଓ ମାର୍ଗଦର୍ଶନ।",
    loading: "ଶିକ୍ଷା ବିଷୟ ଲୋଡ୍ ହେଉଛି...",
    errorTitle: "ଶିକ୍ଷା ବିଷୟ ଲୋଡ୍ ହୋଇପାରିଲା ନାହିଁ",
    retry: "ପୁନଃପ୍ରୟାସ କରନ୍ତୁ",
    emptyTitle: "ବର୍ତ୍ତମାନ କୌଣସି ଶିକ୍ଷା ବିଷୟ ଉପଲବ୍ଧ ନାହିଁ",
    emptyDescription:
      "ଆପଣଙ୍କ ପାଇଁ ବ୍ୟକ୍ତିଗତ ଶିକ୍ଷା ବିଷୟ ତିଆରି ହେବା ପରେ ଏଠାରେ ଦେଖାଯିବ।",
    watchVideo: "ଭିଡିଓ ଦେଖନ୍ତୁ",
    channel: "ଚ୍ୟାନେଲ",
    noVideos: "ଏହି ବିଷୟ ପାଇଁ ବର୍ତ୍ତମାନ କୌଣସି ଭିଡିଓ ଉପଲବ୍ଧ ନାହିଁ।",
    topicLabel: "ବିଷୟ",
  },
};

const Education = () => {
  const { user } = useAuth();
  const preferredLanguage =
    user?.preferredLanguage || localStorage.getItem("language") || "en";
  const labels = uiText[preferredLanguage] || uiText.en;

  const [topics, setTopics] = useState([]);
  const [selectedTopicIndex, setSelectedTopicIndex] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const loadTopics = async () => {
    setLoading(true);
    setError("");

    try {
      const { data } = await educationService.getAIEducation();
      const nextTopics = Array.isArray(data) ? data : [];
      setTopics(nextTopics);
      setSelectedTopicIndex((currentIndex) => {
        if (!nextTopics.length) return 0;
        if (currentIndex >= nextTopics.length) return 0;
        return currentIndex;
      });
    } catch (err) {
      setError(
        err.response?.data?.error ||
          err.response?.data?.message ||
          err.message ||
          "Unable to load learning topics right now.",
      );
      setTopics([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadTopics();
  }, []);

  const selectedTopic = topics[selectedTopicIndex] || null;

  const topicSummary = useMemo(() => {
    if (!topics.length) return [];
    return topics.map((topic, index) => ({
      ...topic,
      selected: index === selectedTopicIndex,
    }));
  }, [topics, selectedTopicIndex]);

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
            {labels.errorTitle}
          </p>
          <p className="mt-2 text-sm text-slate-600">{error}</p>
          <button
            type="button"
            onClick={loadTopics}
            className="mt-5 rounded-xl bg-emerald-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-emerald-700"
          >
            {labels.retry}
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
            {labels.title}
          </p>
          <h1 className="mt-2 text-3xl font-bold">{labels.headline}</h1>
          <p className="mt-2 text-sm text-amber-50">{labels.subtitle}</p>
        </section>

        {!topics.length ? (
          <section className="rounded-3xl border border-dashed border-slate-200 bg-white p-8 text-center shadow-sm">
            <p className="text-xl font-semibold text-slate-900">
              {labels.emptyTitle}
            </p>
            <p className="mt-2 text-sm text-slate-600">
              {labels.emptyDescription}
            </p>
            <button
              type="button"
              onClick={loadTopics}
              className="mt-5 rounded-xl bg-emerald-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-emerald-700"
            >
              {labels.retry}
            </button>
          </section>
        ) : (
          <section className="grid gap-6 xl:grid-cols-[0.9fr_1.1fr]">
            <div className="space-y-4">
              {topicSummary.map((topic, index) => (
                <button
                  key={`${topic.title}-${index}`}
                  type="button"
                  onClick={() => setSelectedTopicIndex(index)}
                  className={`w-full rounded-3xl border p-4 text-left shadow-sm transition ${
                    topic.selected
                      ? "border-emerald-300 bg-emerald-50"
                      : "border-slate-200 bg-white hover:bg-slate-50"
                  }`}
                >
                  <div className="flex items-start justify-between gap-3">
                    <div>
                      <p className="text-xs uppercase tracking-wide text-slate-500">
                        {labels.topicLabel}
                      </p>
                      <h2 className="mt-2 text-lg font-semibold text-slate-900">
                        {topic.title}
                      </h2>
                    </div>
                    <span className="rounded-full bg-amber-100 px-2 py-1 text-[10px] font-semibold uppercase tracking-wide text-amber-700">
                      {index + 1}
                    </span>
                  </div>
                  <p className="mt-3 line-clamp-3 text-sm text-slate-600">
                    {topic.reason}
                  </p>
                </button>
              ))}
            </div>

            <div className="rounded-3xl bg-white p-5 shadow-sm ring-1 ring-slate-100">
              {selectedTopic ? (
                <>
                  <p className="text-xs uppercase tracking-[0.2em] text-slate-500">
                    {labels.topicLabel}
                  </p>
                  <h2 className="mt-3 text-2xl font-bold text-slate-900">
                    {selectedTopic.title}
                  </h2>
                  <div className="mt-5 rounded-2xl bg-amber-50 p-4">
                    <p className="text-sm leading-7 text-slate-700 whitespace-pre-line">
                      {selectedTopic.reason}
                    </p>
                  </div>

                  <div className="mt-6 space-y-4">
                    {selectedTopic.videos && selectedTopic.videos.length ? (
                      selectedTopic.videos.slice(0, 3).map((video) => (
                        <div
                          key={video.videoId || video.url}
                          className="overflow-hidden rounded-2xl border border-slate-200 bg-slate-50 shadow-sm"
                        >
                          <div className="relative">
                            {video.thumbnail ? (
                              <img
                                src={video.thumbnail}
                                alt={video.title || "YouTube video thumbnail"}
                                className="h-44 w-full object-cover"
                              />
                            ) : (
                              <div className="flex h-44 w-full items-center justify-center bg-slate-200 text-sm text-slate-500">
                                Video
                              </div>
                            )}
                          </div>

                          <div className="p-4">
                            <h3 className="text-base font-semibold text-slate-900">
                              {video.title || "YouTube video"}
                            </h3>
                            <p className="mt-2 text-sm text-slate-500">
                              {labels.channel}:{" "}
                              {video.channelTitle || "Agriculture channel"}
                            </p>
                            {video.description ? (
                              <p className="mt-2 line-clamp-3 text-sm text-slate-600">
                                {video.description}
                              </p>
                            ) : null}
                            {video.url ? (
                              <a
                                href={video.url}
                                target="_blank"
                                rel="noreferrer"
                                className="mt-4 inline-flex rounded-xl bg-emerald-600 px-4 py-2 text-sm font-medium text-white hover:bg-emerald-700"
                              >
                                {labels.watchVideo}
                              </a>
                            ) : null}
                          </div>
                        </div>
                      ))
                    ) : (
                      <div className="rounded-2xl border border-dashed border-slate-200 bg-slate-50 p-4 text-sm text-slate-600">
                        {labels.noVideos}
                      </div>
                    )}
                  </div>
                </>
              ) : null}
            </div>
          </section>
        )}
      </div>
    </div>
  );
};

export default Education;
