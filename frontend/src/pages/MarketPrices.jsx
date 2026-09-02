import { useAuth } from "../context/AuthContext.jsx";
import {
  cropService,
  farmerService,
  marketService,
  marketAdviceService,
} from "../services/api.js";
import { useEffect, useMemo, useState } from "react";

const currencyFormatter = new Intl.NumberFormat("en-IN", {
  style: "currency",
  currency: "INR",
  minimumFractionDigits: 0,
  maximumFractionDigits: 0,
});

const labelsByLanguage = {
  en: {
    marketDashboard: "Market Dashboard",
    subtitle:
      "Latest market prices for your crops. All prices are sourced from our verified market dataset. Verify the latest price with your local mandi before selling.",
    myCrops: "My Crops",
    selectCrop: "Select a crop to view prices",
    cropName: "Crop name",
    state: "State",
    district: "District",
    applyFilters: "Apply Filters",
    clearFilters: "Clear",
    priceSummary: "Price Summary",
    bestPrice: "Best Available Price",
    lowestPrice: "Lowest Available Price",
    yourAreaPrice: "Your Area Price",
    priceUnit: "/ quintal",
    marketComparison: "Market Comparison",
    market: "Market",
    location: "Location",
    unit: "Unit",
    date: "Date",
    priceTrend: "Price Trend",
    aiInsight: "AI Market Interpretation",
    aiDisclaimerShort: "Based on real market data",
    aiDisclaimer:
      "AI-generated interpretation based on available market data. Verify the latest price with your local mandi before selling.",
    noInsight: "AI market interpretation is not available right now.",
    retry: "Try again",
    loading: "Loading market data...",
    noCrops:
      "No crops are currently added to your profile. Add your crops to view market prices.",
    noCropsHint: "Go to My Crops to add your crops.",
    farmLocation: "Farm location",
    compareHint: "Real backend market comparison for this crop",
    noComparison: "No comparison data is available for this crop.",
    noTrend: "Trend data is unavailable for this crop.",
    noPrices: "No market prices found",
    noPricesForCrop: "No market data available for this crop",
    noPricesForDistrict: "No market data available for your district",
    noPricesForState: "No market data available for your state",
    noPricesHint:
      "Try another crop, state, or district to view available market prices.",
    dataSource: "Market prices are sourced from our verified market dataset.",
    notAvailable: "Not available",
    loadingPrices: "Loading prices...",
    loadingInsight: "Loading insight...",
    highestPrice: "Highest price",
    lowestAvailable: "Lowest price",
    yourDistrict: "Your district",
    selectACrop: "Select a crop to see market data for your area.",
  },
  hi: {
    marketDashboard: "बाजार डैशबोर्ड",
    subtitle:
      "आपकी फसलों के लिए नवीनतम बाजार कीमतें। सभी कीमतें हमारे सत्यापित बाजार डेटासेट से ली गई हैं। बेचने से पहले अपने स्थानीय मंडी से नवीनतम कीमत सत्यापित करें।",
    myCrops: "मेरी फसलें",
    selectCrop: "कीमतें देखने के लिए एक फसल चुनें",
    cropName: "फसल का नाम",
    state: "राज्य",
    district: "जिला",
    applyFilters: "फिल्टर लागू करें",
    clearFilters: "साफ करें",
    priceSummary: "कीमत सारांश",
    bestPrice: "सर्वश्रेष्ठ उपलब्ध कीमत",
    lowestPrice: "सबसे कम उपलब्ध कीमत",
    yourAreaPrice: "आपके क्षेत्र की कीमत",
    priceUnit: "/ क्विंटल",
    marketComparison: "बाजार तुलना",
    market: "बाजार",
    location: "स्थान",
    unit: "इकाई",
    date: "तारीख",
    priceTrend: "कीमत का रुझान",
    aiInsight: "AI बाजार व्याख्या",
    aiDisclaimerShort: "वास्तविक बाजार डेटा के आधार पर",
    aiDisclaimer:
      "उपलब्ध बाजार डेटा के आधार पर AI-जनित व्याख्या। बेचने से पहले अपने स्थानीय मंडी से नवीनतम कीमत सत्यापित करें।",
    noInsight: "AI बाजार व्याख्या अभी उपलब्ध नहीं है।",
    retry: "फिर से प्रयास करें",
    loading: "बाजार डेटा लोड हो रहा है...",
    noCrops:
      "आपकी प्रोफ़ाइल में अभी कोई फसल जोड़ी नहीं गई है। कीमतें देखने के लिए अपनी फसलें जोड़ें।",
    noCropsHint: "अपनी फसलें जोड़ने के लिए मेरी फसलें पर जाएं।",
    farmLocation: "खेत का स्थान",
    compareHint: "इस फसल के लिए वास्तविक बैकएंड बाजार तुलना",
    noComparison: "इस फसल के लिए तुलना डेटा उपलब्ध नहीं है।",
    noTrend: "इस फसल के लिए रुझान डेटा उपलब्ध नहीं है।",
    noPrices: "कोई बाजार कीमत नहीं मिली",
    noPricesForCrop: "इस फसल के लिए कोई बाजार डेटा उपलब्ध नहीं है",
    noPricesForDistrict: "आपके जिले के लिए कोई बाजार डेटा उपलब्ध नहीं है",
    noPricesForState: "आपके राज्य के लिए कोई बाजार डेटा उपलब्ध नहीं है",
    noPricesHint:
      "उपलब्ध बाजार कीमतें देखने के लिए दूसरी फसल, राज्य या जिला आजमाएं।",
    dataSource: "बाजार कीमतें हमारे सत्यापित बाजार डेटासेट से ली गई हैं।",
    notAvailable: "उपलब्ध नहीं",
    loadingPrices: "कीमतें लोड हो रही हैं...",
    loadingInsight: "अंतर्दृष्टि लोड हो रही है...",
    highestPrice: "उच्चतम कीमत",
    lowestAvailable: "न्यूनतम कीमत",
    yourDistrict: "आपका जिला",
    selectACrop: "आपके क्षेत्र के लिए बाजार डेटा देखने के लिए एक फसल चुनें।",
  },
  or: {
    marketDashboard: "ବଜାର ଡ୍ୟାଶବୋର୍ଡ",
    subtitle:
      "ଆପଣଙ୍କ ଫସଲ ପାଇଁ ନବୀନତମ ବଜାର ମୂଲ୍ୟ। ସମସ୍ତ ମୂଲ୍ୟ ଆମର ସତ୍ୟାପିତ ବଜାର ଡେଟାସେଟ ଠାରୁ ସୋର୍ସ। ବିକ୍ରୟ କରିବା ପୂର୍ବରୁ ଆପଣଙ୍କ ସ୍ଥାନୀୟ ମଣ୍ଡିରୁ ସର୍ବଶେଷ ମୂଲ୍ୟ ଯାଞ୍ଚ କରନ୍ତୁ।",
    myCrops: "ମୋର ଫସଲ",
    selectCrop: "ମୂଲ୍ୟ ଦେଖିବା ପାଇଁ ଏକ ଫସଲ ବାଛନ୍ତୁ",
    cropName: "ଫସଲ ନାମ",
    state: "ରାଜ୍ୟ",
    district: "ଜିଲ୍ଲା",
    applyFilters: "ଫିଲ୍ଟର ପ୍ରୟୋଗ କରନ୍ତୁ",
    clearFilters: "ସଫା କରନ୍ତୁ",
    priceSummary: "ମୂଲ୍ୟ ସାରାଂଶ",
    bestPrice: "ସର୍ବୋତ୍ତମ ଉପଲବ୍ଧ ମୂଲ୍ୟ",
    lowestPrice: "ସବୁଠାରୁ କମ୍ ଉପଲବ୍ଧ ମୂଲ୍ୟ",
    yourAreaPrice: "ଆପଣଙ୍କ ଏଲାକାର ମୂଲ୍ୟ",
    priceUnit: "/ କ୍ବିନ୍ଟାଲ୍",
    marketComparison: "ବଜାର ତୁଳନା",
    market: "ବଜାର",
    location: "ସ୍ଥାନ",
    unit: "ଏକକ",
    date: "ତାରିଖ",
    priceTrend: "ମୂଲ୍ୟ ପ୍ରବୃତ୍ତି",
    aiInsight: "AI ବଜାର ବ୍ୟାଖ୍ୟା",
    aiDisclaimerShort: "ବାସ୍ତବ ବଜାର ଡେଟା ଉପରେ ଆଧାରିତ",
    aiDisclaimer:
      "ଉପଲବ୍ଧ ବଜାର ଡେଟା ଉପରେ ଆଧାରିତ AI-ଜନିତ ବ୍ୟାଖ୍ୟା। ବିକ୍ରୟ କରିବା ପୂର୍ବରୁ ଆପଣଙ୍କ ସ୍ଥାନୀୟ ମଣ୍ଡିରୁ ସର୍ବଶେଷ ମୂଲ୍ୟ ଯାଞ୍ଚ କରନ୍ତୁ।",
    noInsight: "AI ବଜାର ବ୍ୟାଖ୍ୟା ବର୍ତ୍ତମାନ ଉପଲବ୍ଧ ନାହିଁ।",
    retry: "ପୁନ୍ରାୟାସ କରନ୍ତୁ",
    loading: "ବଜାର ଡେଟା ଲୋଡ୍ ହେଉଛି...",
    noCrops:
      "ଆପଣଙ୍କ ପ୍ରୋଫାଇଲରେ ବର୍ତ୍ତମାନ କୌଣସି ଫସଲ ଯୋଗ କରାଯାଇନାହିଁ। ମୂଲ୍ୟ ଦେଖିବା ପାଇଁ ଆପଣଙ୍କ ଫସଲ ଯୋଗ କରନ୍ତୁ।",
    noCropsHint: "ଆପଣଙ୍କ ଫସଲ ଯୋଗ କରିବା ପାଇଁ ମୋର ଫସଲ ଯାଆନ୍ତୁ।",
    farmLocation: "କ୍ଷେତ୍ର ସ୍ଥାନ",
    compareHint: "ଏହି ଫସଲର ବାସ୍ତବିକ ବ୍ୟାକଏଣ୍ଡ ବଜାର ତୁଳନା",
    noComparison: "ଏହି ଫସଲ ପାଇଁ ତୁଳନା ଡେଟା ଉପଲବ୍ଧ ନାହିଁ।",
    noTrend: "ଏହି ଫସଲ ପାଇଁ ପ୍ରବୃତ୍ତି ଡେଟା ଉପଲବ୍ଧ ନାହିଁ।",
    noPrices: "କୌଣସି ବଜାର ମୂଲ୍ୟ ମିଳିଲା ନାହିଁ",
    noPricesForCrop: "ଏହି ଫସଲ ପାଇଁ କୌଣସି ବଜାର ଡେଟା ଉପଲବ୍ଧ ନାହିଁ",
    noPricesForDistrict: "ଆପଣଙ୍କ ଜିଲ୍ଲା ପାଇଁ କୌଣସି ବଜାର ଡେଟା ଉପଲବ୍ଧ ନାହିଁ",
    noPricesForState: "ଆପଣଙ୍କ ରାଜ୍ୟ ପାଇଁ କୌଣସି ବଜାର ଡେଟା ଉପଲବ୍ଧ ନାହିଁ",
    noPricesHint:
      "ଉପଲବ୍ଧ ବଜାର ମୂଲ୍ୟ ଦେଖିବା ପାଇଁ ଅନ୍ୟ ଫସଲ, ରାଜ୍ୟ, କିମ୍ବା ଜିଲ୍ଲା ଚେଷ୍ଟା କରନ୍ତୁ।",
    dataSource: "ବଜାର ମୂଲ୍ୟ ଆମର ସତ୍ୟାପିତ ବଜାର ଡେଟାସେଟ ଠାରୁ ସୋର୍ସ।",
    notAvailable: "ଉପଲବ୍ଧ ନାହିଁ",
    loadingPrices: "ମୂଲ୍ୟ ଲୋଡ୍ ହେଉଛି...",
    loadingInsight: "ବ୍ୟାଖ୍ୟା ଲୋଡ୍ ହେଉଛି...",
    highestPrice: "ସର୍ବୋଚ୍ଚ ମୂଲ୍ୟ",
    lowestAvailable: "ସବୁଠାରୁ କମ୍ ମୂଲ୍ୟ",
    yourDistrict: "ଆପଣଙ୍କ ଜିଲ୍ଲା",
    selectACrop: "ଆପଣଙ୍କ ଏଲାକାର ବଜାର ଡେଟା ଦେଖିବା ପାଇଁ ଏକ ଫସଲ ବାଛନ୍ତୁ।",
  },
  mr: {
    marketDashboard: "बाजार डॅशबोर्ड",
    subtitle:
      "आपल्या पिकांसाठी नवीनतम बाजार भाव। सर्व भाव आमच्या सत्यापित बाजार डेटासेटमधून घेतले आहेत। विकण्यापूर्वी आपल्या स्थानिक मंडीमध्ये नवीनतम भाव सत्यापित करा।",
    myCrops: "माझ्या पिके",
    selectCrop: "भाव पाहण्यासाठी एक पीक निवडा",
    cropName: "पिकाचे नाव",
    state: "राज्य",
    district: "जिल्हा",
    applyFilters: "फिल्टर लागू करा",
    clearFilters: "पुसून टाका",
    priceSummary: "भाव सारांश",
    bestPrice: "उपलब्ध सर्वोत्तम भाव",
    lowestPrice: "उपलब्ध सर्वनिम्न भाव",
    yourAreaPrice: "आपल्या क्षेत्रातील भाव",
    priceUnit: "/ क्विंटल",
    marketComparison: "बाजार तुलना",
    market: "बाजार",
    location: "स्थान",
    unit: "एकक",
    date: "तारीख",
    priceTrend: "भाव प्रवृत्ती",
    aiInsight: "AI बाजार व्याख्या",
    aiDisclaimerShort: "वास्तविक बाजार डेटावर आधारित",
    aiDisclaimer:
      "उपलब्ध बाजार डेटावर आधारित AI-जनित व्याख्या। विकण्यापूर्वी आपल्या स्थानिक मंडीमध्ये नवीनतम भाव सत्यापित करा।",
    noInsight: "AI बाजार व्याख्या आता उपलब्ध नाही।",
    retry: "पुन्हा प्रयत्न करा",
    loading: "बाजार डेटा लोड होत आहे...",
    noCrops:
      "आपल्या प्रोफाइलमध्ये सध्या कोणतीही पिके नाहीत। भाव पाहण्यासाठी आपली पिके जोडा।",
    noCropsHint: "आपली पिके जोडण्यासाठी माझ्या पिकांकडे जा।",
    farmLocation: "शेताचे स्थान",
    compareHint: "या पिकासाठी वास्तविक बॅकएंड बाजार तुलना",
    noComparison: "या पिकासाठी तुलना डेटा उपलब्ध नाही।",
    noTrend: "या पिकासाठी प्रवृत्ती डेटा उपलब्ध नाही।",
    noPrices: "कोणताही बाजार भाव सापडला नाही",
    noPricesForCrop: "या पिकासाठी कोणताही बाजार डेटा उपलब्ध नाही",
    noPricesForDistrict: "आपल्या जिल्ह्यासाठी कोणताही बाजार डेटा उपलब्ध नाही",
    noPricesForState: "आपल्या राज्यासाठी कोणताही बाजार डेटा उपलब्ध नाही",
    noPricesHint:
      "उपलब्ध बाजार भाव पाहण्यासाठी दुसरी पीक, राज्य किंवा जिल्हा वापरून पहा।",
    dataSource: "बाजार भाव आमच्या सत्यापित बाजार डेटासेटमधून घेतले आहेत।",
    notAvailable: "उपलब्ध नाही",
    loadingPrices: "भाव लोड होत आहेत...",
    loadingInsight: "व्याख्या लोड होत आहे...",
    highestPrice: "सर्वोच्च भाव",
    lowestAvailable: "न्यूनतम भाव",
    yourDistrict: "आपला जिल्हा",
    selectACrop: "आपल्या क्षेत्रातील बाजार डेटा पाहण्यासाठी एक पीक निवडा।",
  },
};

const isValidMarketPrice = (value) => {
  const numeric = Number(value);
  return Number.isFinite(numeric) && numeric > 0 && numeric < 500000;
};

const formatDate = (value) => {
  if (!value) return "—";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return new Intl.DateTimeFormat("en-IN", {
    day: "numeric",
    month: "short",
    year: "numeric",
  }).format(date);
};

const MarketPrices = () => {
  const { user } = useAuth();
  const preferredLanguage =
    user?.preferredLanguage || localStorage.getItem("language") || "en";
  const labels = labelsByLanguage[preferredLanguage] || labelsByLanguage.en;

  const [prices, setPrices] = useState([]);
  const [myCrops, setMyCrops] = useState([]);
  const [farmerProfile, setFarmerProfile] = useState(null);
  const [selectedCrop, setSelectedCrop] = useState("");
  const [marketAdvice, setMarketAdvice] = useState([]);
  const [priceHistory, setPriceHistory] = useState([]);
  const [comparison, setComparison] = useState([]);
  const [filters, setFilters] = useState({
    cropName: "",
    state: "",
    district: "",
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [insightLoading, setInsightLoading] = useState(false);

  // Load farmer profile and crops
  useEffect(() => {
    const initialize = async () => {
      try {
        const [profileResponse, cropsResponse] = await Promise.all([
          farmerService.getProfile().catch(() => null),
          cropService.getCrops().catch(() => []),
        ]);

        const profile = profileResponse?.data || null;
        const cropList = Array.isArray(cropsResponse?.data)
          ? cropsResponse.data
          : [];

        setFarmerProfile(profile);
        setMyCrops(cropList);

        // Pre-select first crop if available
        if (cropList.length > 0) {
          const firstCrop = cropList[0].cropName || "";
          setSelectedCrop(firstCrop);
          setFilters({
            cropName: firstCrop,
            state: profile?.state || "",
            district: profile?.district || "",
          });
          await loadPricesForCrop(
            firstCrop,
            profile?.state || "",
            profile?.district || "",
          );
        } else {
          setLoading(false);
        }

        // Load market insights
        await loadMarketInsights();
      } catch (err) {
        setError("Unable to load market context.");
        setLoading(false);
      }
    };

    initialize();
  }, []);

  const loadPricesForCrop = async (cropName, state, district) => {
    setLoading(true);
    setError("");

    try {
      const { data } = await marketService.getPrices({
        cropName,
        state: state || undefined,
        district: district || undefined,
      });
      setPrices(Array.isArray(data) ? data : []);

      // Load history and comparison
      const [historyResponse, compareResponse] = await Promise.allSettled([
        marketService.getPriceHistory({
          cropName,
          state: state || undefined,
        }),
        marketService.comparePrices({
          cropName,
          state: state || undefined,
          district: district || undefined,
        }),
      ]);

      if (historyResponse.status === "fulfilled") {
        setPriceHistory(
          Array.isArray(historyResponse.value?.data)
            ? historyResponse.value.data
            : [],
        );
      }

      if (compareResponse.status === "fulfilled") {
        setComparison(
          Array.isArray(compareResponse.value?.data)
            ? compareResponse.value.data
            : [],
        );
      }
    } catch (err) {
      setError(
        err.response?.data?.message ||
          err.message ||
          "Unable to load market prices.",
      );
      setPrices([]);
      setPriceHistory([]);
      setComparison([]);
    } finally {
      setLoading(false);
    }
  };

  const loadMarketInsights = async () => {
    setInsightLoading(true);

    try {
      const { data } = await marketAdviceService.getMarketAdvice();
      setMarketAdvice(Array.isArray(data) ? data : []);
    } catch {
      setMarketAdvice([]);
    } finally {
      setInsightLoading(false);
    }
  };

  const handleCropSelect = (cropName) => {
    setSelectedCrop(cropName);
    setFilters({
      cropName,
      state: farmerProfile?.state || "",
      district: farmerProfile?.district || "",
    });
    loadPricesForCrop(
      cropName,
      farmerProfile?.state || "",
      farmerProfile?.district || "",
    );
  };

  const handleApplyFilters = async (e) => {
    e.preventDefault();
    const nextFilters = {
      cropName: filters.cropName?.trim() || "",
      state: filters.state?.trim() || "",
      district: filters.district?.trim() || "",
    };
    if (nextFilters.cropName) {
      setSelectedCrop(nextFilters.cropName);
      await loadPricesForCrop(
        nextFilters.cropName,
        nextFilters.state,
        nextFilters.district,
      );
    }
  };

  // Calculate price summary
  const priceSummary = useMemo(() => {
    const validPrices = prices
      .map((p) => Number(p.modalPrice))
      .filter(isValidMarketPrice);

    if (!validPrices.length) {
      return {
        best: null,
        lowest: null,
        areaPrice: null,
      };
    }

    const best = Math.max(...validPrices);
    const lowest = Math.min(...validPrices);

    // Find area price (district/state match)
    const areaPriceEntry = prices.find(
      (p) =>
        (p.district === farmerProfile?.district ||
          p.state === farmerProfile?.state) &&
        isValidMarketPrice(p.modalPrice),
    );
    const areaPrice = areaPriceEntry ? Number(areaPriceEntry.modalPrice) : null;

    return { best, lowest, areaPrice };
  }, [prices, farmerProfile]);

  if (loading && !myCrops.length) {
    return (
      <div className="min-h-screen bg-emerald-50 px-4 py-6">
        <div className="mx-auto max-w-6xl space-y-4">
          <div className="animate-pulse space-y-4">
            <div className="h-48 rounded-3xl bg-white" />
            <div className="grid gap-4 md:grid-cols-3">
              {[1, 2, 3].map((i) => (
                <div key={i} className="h-32 rounded-2xl bg-white" />
              ))}
            </div>
          </div>
        </div>
      </div>
    );
  }

  if (!myCrops.length) {
    return (
      <div className="min-h-screen bg-emerald-50 px-4 py-6">
        <div className="mx-auto max-w-3xl space-y-6">
          <section className="rounded-3xl bg-gradient-to-r from-violet-600 to-indigo-500 p-6 text-white shadow-lg">
            <h1 className="text-3xl font-bold">{labels.marketDashboard}</h1>
            <p className="mt-2 text-sm text-violet-50">{labels.subtitle}</p>
          </section>
          <div className="rounded-2xl border border-amber-200 bg-amber-50 p-6 text-center">
            <p className="text-lg font-semibold text-slate-900">
              {labels.noCrops}
            </p>
            <p className="mt-2 text-sm text-slate-600">{labels.noCropsHint}</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-emerald-50 px-4 py-6">
      <div className="mx-auto max-w-6xl space-y-6">
        {/* Header */}
        <section className="rounded-3xl bg-gradient-to-r from-violet-600 to-indigo-500 p-6 text-white shadow-lg">
          <p className="text-sm font-medium uppercase tracking-widest text-violet-100">
            Market
          </p>
          <h1 className="mt-2 text-3xl font-bold">{labels.marketDashboard}</h1>
          <p className="mt-2 text-sm text-violet-50">{labels.subtitle}</p>
        </section>

        {/* My Crops Section */}
        <section className="rounded-3xl bg-white p-5 shadow-sm ring-1 ring-slate-100">
          <div className="mb-4 flex flex-col gap-3">
            <div>
              <p className="text-lg font-semibold text-slate-900">
                {labels.myCrops}
              </p>
              {farmerProfile && (
                <p className="text-xs text-slate-500">
                  {labels.farmLocation}: {farmerProfile.district || "—"},{" "}
                  {farmerProfile.state || "—"}
                </p>
              )}
            </div>
            <div className="flex flex-wrap gap-2">
              {myCrops.map((crop) => (
                <button
                  key={crop.id || crop.cropName}
                  type="button"
                  onClick={() => handleCropSelect(crop.cropName || "")}
                  className={`rounded-full px-4 py-2 text-sm font-medium transition ${
                    selectedCrop === crop.cropName
                      ? "bg-emerald-600 text-white"
                      : "bg-emerald-50 text-emerald-700 hover:bg-emerald-100"
                  }`}
                >
                  {crop.cropName}
                </button>
              ))}
            </div>
          </div>

          <form
            onSubmit={handleApplyFilters}
            className="grid gap-3 md:grid-cols-4"
          >
            <input
              type="text"
              value={filters.cropName}
              onChange={(e) =>
                setFilters((current) => ({
                  ...current,
                  cropName: e.target.value,
                }))
              }
              placeholder={labels.cropName}
              className="rounded-xl border border-slate-300 px-3 py-3 text-sm outline-none focus:border-emerald-500 focus:ring-2 focus:ring-emerald-100"
            />
            <input
              type="text"
              value={filters.state}
              onChange={(e) =>
                setFilters((current) => ({
                  ...current,
                  state: e.target.value,
                }))
              }
              placeholder={labels.state}
              className="rounded-xl border border-slate-300 px-3 py-3 text-sm outline-none focus:border-emerald-500 focus:ring-2 focus:ring-emerald-100"
            />
            <input
              type="text"
              value={filters.district}
              onChange={(e) =>
                setFilters((current) => ({
                  ...current,
                  district: e.target.value,
                }))
              }
              placeholder={labels.district}
              className="rounded-xl border border-slate-300 px-3 py-3 text-sm outline-none focus:border-emerald-500 focus:ring-2 focus:ring-emerald-100"
            />
            <div className="flex gap-2">
              <button
                type="submit"
                className="flex-1 rounded-xl bg-violet-600 px-4 py-3 text-sm font-semibold text-white hover:bg-violet-700"
              >
                {labels.applyFilters}
              </button>
              <button
                type="button"
                onClick={() => {
                  setFilters({
                    cropName: selectedCrop,
                    state: farmerProfile?.state || "",
                    district: farmerProfile?.district || "",
                  });
                  loadPricesForCrop(
                    selectedCrop,
                    farmerProfile?.state || "",
                    farmerProfile?.district || "",
                  );
                }}
                className="rounded-xl border border-slate-300 px-4 py-3 text-sm font-medium text-slate-700 hover:bg-slate-50"
              >
                {labels.clearFilters}
              </button>
            </div>
          </form>
        </section>

        {/* Error State */}
        {error && (
          <section className="rounded-2xl border border-red-200 bg-red-50 p-6 text-center">
            <p className="text-lg font-semibold text-red-700">{error}</p>
            <button
              onClick={() =>
                loadPricesForCrop(selectedCrop, filters.state, filters.district)
              }
              className="mt-4 rounded-xl bg-red-600 px-4 py-2 text-sm font-medium text-white hover:bg-red-700"
            >
              {labels.retry}
            </button>
          </section>
        )}

        {/* Price Summary Cards */}
        {!error && !loading && (
          <>
            <section className="grid gap-4 md:grid-cols-3">
              {/* Best Price */}
              <div className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-100">
                <p className="text-xs font-medium uppercase tracking-wide text-slate-500">
                  {labels.bestPrice}
                </p>
                <p className="mt-4 text-3xl font-bold text-emerald-700">
                  {priceSummary.best
                    ? currencyFormatter.format(priceSummary.best)
                    : labels.notAvailable}
                </p>
                {priceSummary.best && (
                  <p className="mt-2 text-xs text-slate-500">
                    {labels.priceUnit}
                  </p>
                )}
              </div>

              {/* Lowest Price */}
              <div className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-100">
                <p className="text-xs font-medium uppercase tracking-wide text-slate-500">
                  {labels.lowestPrice}
                </p>
                <p className="mt-4 text-3xl font-bold text-amber-700">
                  {priceSummary.lowest
                    ? currencyFormatter.format(priceSummary.lowest)
                    : labels.notAvailable}
                </p>
                {priceSummary.lowest && (
                  <p className="mt-2 text-xs text-slate-500">
                    {labels.priceUnit}
                  </p>
                )}
              </div>

              {/* Your Area Price */}
              <div className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-100">
                <p className="text-xs font-medium uppercase tracking-wide text-slate-500">
                  {labels.yourAreaPrice}
                </p>
                <p className="mt-4 text-3xl font-bold text-slate-900">
                  {priceSummary.areaPrice
                    ? currencyFormatter.format(priceSummary.areaPrice)
                    : labels.notAvailable}
                </p>
                {priceSummary.areaPrice && (
                  <p className="mt-2 text-xs text-slate-500">
                    {labels.priceUnit}
                  </p>
                )}
              </div>
            </section>

            {/* AI Insight */}
            <section className="rounded-3xl bg-white p-5 shadow-sm ring-1 ring-slate-100">
              <div className="flex items-center justify-between gap-3">
                <div>
                  <p className="text-lg font-bold text-slate-900">
                    {labels.aiInsight}
                  </p>
                  <p className="text-xs text-slate-500">
                    {labels.aiDisclaimerShort}
                  </p>
                </div>
                {insightLoading && (
                  <span className="text-xs text-slate-500">
                    {labels.loadingInsight}
                  </span>
                )}
              </div>

              {marketAdvice.length ? (
                <div className="mt-4 space-y-4">
                  {marketAdvice.map((item, idx) => (
                    <article
                      key={idx}
                      className="rounded-2xl border border-slate-200 bg-slate-50 p-4"
                    >
                      <p className="font-semibold text-slate-900">
                        {item.crop || selectedCrop}
                      </p>
                      <p className="mt-2 text-sm text-slate-700">
                        {item.summary}
                      </p>
                      {item.trend && (
                        <p className="mt-2 text-sm text-slate-600">
                          <span className="font-medium">
                            {labels.priceTrend}:
                          </span>{" "}
                          {item.trend}
                        </p>
                      )}
                      <p className="mt-2 text-xs text-amber-600">
                        {labels.aiDisclaimer}
                      </p>
                    </article>
                  ))}
                </div>
              ) : (
                <div className="mt-4 rounded-2xl border border-dashed border-slate-200 bg-slate-50 p-4 text-sm text-slate-600">
                  {labels.noInsight}
                </div>
              )}
            </section>

            {/* Price Trend & Comparison */}
            {(priceHistory.length > 0 || comparison.length > 0) && (
              <section className="grid gap-4 xl:grid-cols-2">
                {priceHistory.length > 0 && (
                  <div className="rounded-3xl bg-white p-5 shadow-sm ring-1 ring-slate-100">
                    <p className="text-lg font-bold text-slate-900">
                      {labels.priceTrend}
                    </p>
                    <div className="mt-4 space-y-3">
                      {priceHistory.slice(0, 5).map((item, idx) => (
                        <div
                          key={idx}
                          className="flex items-center justify-between rounded-xl bg-slate-50 px-3 py-2 text-sm"
                        >
                          <span className="text-slate-600">
                            {formatDate(item.arrivalDate)}
                          </span>
                          <span className="font-semibold text-slate-900">
                            {currencyFormatter.format(item.modalPrice)}
                          </span>
                        </div>
                      ))}
                    </div>
                  </div>
                )}

                {comparison.length > 0 && (
                  <div className="rounded-3xl bg-white p-5 shadow-sm ring-1 ring-slate-100">
                    <p className="text-lg font-bold text-slate-900">
                      {labels.marketComparison}
                    </p>
                    <p className="mt-1 text-xs text-slate-500">
                      {labels.compareHint}
                    </p>
                    <div className="mt-4 space-y-3">
                      {comparison.slice(0, 5).map((item, idx) => (
                        <div
                          key={idx}
                          className="flex items-center justify-between rounded-xl bg-slate-50 px-3 py-2 text-sm"
                        >
                          <div>
                            <p className="font-semibold text-slate-900">
                              {item.market || "Market"}
                            </p>
                            <p className="text-xs text-slate-500">
                              {item.district}, {item.state}
                            </p>
                          </div>
                          <div className="text-right">
                            <p className="font-semibold text-slate-900">
                              {currencyFormatter.format(item.modalPrice)}
                            </p>
                            <p className="text-xs text-slate-500">
                              {item.unit}
                            </p>
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                )}
              </section>
            )}

            {/* No Data Message */}
            {prices.length === 0 && (
              <section className="rounded-2xl border border-dashed border-slate-300 bg-white p-8 text-center">
                <p className="text-lg font-semibold text-slate-900">
                  {selectedCrop ? labels.noPricesForCrop : labels.noPrices}
                </p>
                <p className="mt-2 text-sm text-slate-600">
                  {labels.noPricesHint}
                </p>
              </section>
            )}

            {/* Data Source Disclaimer */}
            <div className="rounded-2xl bg-blue-50 p-4 text-xs text-slate-600 ring-1 ring-blue-100">
              <p>
                <span className="font-medium text-slate-900">Data Source:</span>{" "}
                {labels.dataSource}
              </p>
            </div>
          </>
        )}
      </div>
    </div>
  );
};

export default MarketPrices;
