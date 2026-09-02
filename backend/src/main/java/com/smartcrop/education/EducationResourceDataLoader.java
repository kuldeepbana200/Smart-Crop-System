package com.smartcrop.education;

import com.smartcrop.education.entity.EducationResource;
import com.smartcrop.education.repository.EducationResourceRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Profile("!prod")
public class EducationResourceDataLoader implements ApplicationRunner {

        private final EducationResourceRepository educationResourceRepository;

        public EducationResourceDataLoader(EducationResourceRepository educationResourceRepository) {
                this.educationResourceRepository = educationResourceRepository;
        }

        @Override
        public void run(ApplicationArguments args) throws Exception {
                // Only seed if table is empty to avoid duplicates
                if (educationResourceRepository.count() > 0) {
                        return;
                }

                List<EducationResource> resources = new ArrayList<>();

                // English resources
                resources.add(createResource("en", "crop_basics", "Understanding Soil Types for Better Crop Yield",
                                "Learn how to identify sandy, loam, and clay soils and which crops are best suited for each type. Includes simple soil testing methods you can do in the field.",
                                null));
                resources.add(createResource("en", "irrigation", "Drip Irrigation Basics for Small Farms",
                                "Step-by-step guide to setting up a low-cost drip irrigation system, water scheduling, and maintenance tips for smallholder farmers.",
                                null));
                resources.add(createResource("en", "pests_diseases", "Identifying and Managing Fall Armyworm",
                                "Describes the life cycle, damage signs, scouting methods, and integrated pest management options for controlling fall armyworm in maize and other crops.",
                                null));
                resources.add(createResource("en", "soil_health", "How to Improve Soil Organic Matter",
                                "Covers composting, green manure, crop residues, and reduced tillage practices to boost soil fertility and structure.",
                                null));
                resources.add(createResource("en", "weather", "Protecting Crops from Unexpected Frost",
                                "Practical measures like mulching, windbreaks, and irrigation timing to reduce frost damage to sensitive crops.",
                                null));
                resources.add(createResource("en", "market_storage", "Best Practices for Grain Storage to Avoid Losses",
                                "Explains cleaning, drying, hermetic storage, and regular monitoring to prevent mold, insects, and grain quality deterioration.",
                                null));

                // Hindi resources
                resources.add(createResource("hi", "crop_basics", "फसल उत्पादन के लिए मिट्टी के प्रकार को समझें",
                                "बालू, दोमट, और बलुई मिट्टी की पहचान करना और प्रत्येक प्रकार के लिए उपयुक्त फसलों के बारे में जानें। क्षेत्र में मिट्टी परीक्षण के सरल तरीके शामिल हैं।",
                                null));
                resources.add(createResource("hi", "irrigation", "छोटे खेतों के लिए ड्रिप सिंचाई की मूल बातें",
                                "छोटे किसानों के लिए कम लागत वाली ड्रिप सिंचाई प्रणाली स्थापित करने, पानी की अनुसूची निर्धारित करने और रखरखाव के सुझावों का चरण-दर-चरण मार्गदर्शिका।",
                                null));
                resources.add(createResource("hi", "pests_diseases", "फॉल आर्मीवर्म की पहचान और प्रबंधन",
                                "मकई और अन्य फसलों में फॉल आर्मीवर्म के जीवन चक्र, क्षति के लक्षण, स्काउटिंग विधियों और एकीकृत कीट प्रबंधन विकल्पों का विवरण।",
                                null));
                resources.add(createResource("hi", "soil_health", "मिट्टी में जैविक पदार्थ बढ़ाने के तरीके",
                                "कम्पोस्ट, हरित खाद, फसल अवशेषों और कम जुताई से मिट्टी की उर्वरता और संरचना में सुधार के तरीके।",
                                null));
                resources.add(createResource("hi", "weather", "अप्रत्याशित फ्रॉस्ट से फसलों की सुरक्षा",
                                "मल्चिंग, विंडब्रेक और सिंचाई के समय के माध्यम से संवेदनशील फसलों पर फ्रॉस्ट क्षति को कम करने के व्यावहारिक उपाय।",
                                null));
                resources.add(createResource("hi", "market_storage",
                                "अनाज भंडारण में नुकसान कम करने के सर्वोत्तम तरीके",
                                "सफाई, सुखाने, वायुरोधी भंडारण और नियमित निगरानी से कवक, कीटों और अनाज गुणवत्ता में गिरावट को रोकने के तरीके।",
                                null));

                // Odia resources
                resources.add(createResource("or", "crop_basics", "ଫସଳୁତ୍ପାଦନ ପାଇଁ ମାଟିର ପ୍ରକାରକୁ ବୁଝିବେ",
                                "ବାଲୁ, ଲୋମ, ଏବଂ ମଟ୍ଟି ମାଟିର ପ୍ରକାରଗୁଡ଼ିକରେ ପରିଚୟ ପାଇଁ କେତେପାକି ମାଟିରେ ଟେସ୍ଟିଙ୍ଗ କରିବାର ସାଧାରଣ ପଦଧତି।",
                                null));
                resources.add(createResource("or", "irrigation", "ଛୋଟ ଖେତରେ ଡ୍ରିପ୍ ସିଞ୍ଚନ ମୂଳବତ୍ତା",
                                "କମ୍ ଳବ୍ ମୂଲ୍ୟର ଡ୍ରିପ୍ ସିଞ୍ଚନ ସିଷ୍ଟେମ ସ୍ଥାପନ, ପାଣି ଶେଡ୍ୱୂଲିଂ, ଏବଂ ରକ୍ଷାନ୍ତି ତତ୍ତ୍ବଗୁଡ଼ିକରେ ସ୍ଥେପନ ଏବଂ ରକ୍ଷାନ୍ତିରେ ଚାଳନା।",
                                null));
                resources.add(createResource("or", "pests_diseases", "ଫୋଲ୍ ଆର୍ମୀୱର୍ମ୍ ପରିଚୟ ଏବଂ ପରିମାଣ",
                                "ମକାଇ ଏବଂ ଅନ୍ୟ ଫସଳରେ ଫୋଲ୍ ଆର୍ମୀୱର୍ମ୍ରର ଜୀବନ ଚକ୍ର, ହାନି ଲକ୍ଷଣ, ସ୍କାଉଟିଙ୍ ପଦଥି, ଏବଂ ଏକାଇଟେଡ୍ ପେଷ୍ଟ୍ ମ୍ୟାନେଜେମେଣ୍ଟ ଉପାୟ।",
                                null));
                resources.add(createResource("or", "soil_health", "ମାଟିରେ ଜୀବିକ ପଦାର୍ଥ ବୃଦ୍ଧିର ଉପାୟ",
                                "କମ୍ପୋସ୍ଟ, ହରିତ ଖାଦ୍, ଅବଶେଷ ପରିଚାଳନ, ଏବଂ କମ୍ ଜୁଟାଇ ଦ୍ୱାରା ଜରୁପର୍ତ୍ତୀକରଣ ବୃଦ୍ଧି।",
                                null));
                resources.add(createResource("or", "weather", "ଅସମାନ୍ନ ଫ୍ରୋଷ୍ଟରୁ ଫସଳର ସୁରକ୍ଷ୍ତି",
                                "ମଲ୍ଚିଙ୍, ବେଣ୍ଡ୍ବ୍ରେକ୍, ଏବଂ ସିଞ୍ଚନ ସମୟରେକ୍ତେଙ୍କୁ ଦ୍ୱାରା ଫ୍ରୋଷ୍ଟ ହାନି କମ୍ କରିବେ।",
                                null));
                resources.add(createResource("or", "market_storage", "ହାନି କମ୍ କରି ଧାନ୍ୟ ଭାଣ୍ଡାରଣର ଶ୍ରେଷ୍ଠ ପାଦତିକା",
                                "ଶୋଚନ, ଶୁଷ୍କ କରିବେ, ବାୟୁରୋଧୀ ଭାଣ୍ଡା, ଏବଂ ନିରନ୍ତର ନିଗ୍ରହନ ଦ୍ୱାରା କ୍ବେଜ୍ ଏବଂ କୀଟରୁ ସୁରକ୍ଷ୍ତି।",
                                null));

                // Marathi resources
                resources.add(createResource("mr", "crop_basics", "शेतीसाठी मिट्टींचे प्रकार ओळखणे",
                                "मिट्टींचे प्रकार (बाळ्ड, दोमट, बलुई) ओळखणे, त्यांचे गुणधर्म जाणणे, आणि शेतीसाठी योग्य प्रकारची ओळख. क्षेत्रात मिट्टी तपासणे करण्याचे सोपा पद्धती.",
                                null));
                resources.add(createResource("mr", "irrigation", "लहान शेतकरींसाठी ड्रिप इर्रिगेशन मूलभूत",
                                "कम खर्चाचा ड्रिप सिस्टम स्थापित करणे, पाणी वगळण्याचा रेट तयार करणे, वसतीचा अंदाज, व देखभालच्या टिप्स.",
                                null));
                resources.add(createResource("mr", "pests_diseases", "फॉल आर्मीवर्मची ओळख व व्यवस्थापन",
                                "जीवनचक्र, क्षतीचे लक्षण, स्कूटिंग पद्धती, व एकीकृत कीट व्यवस्थापन उपाय.",
                                null));
                resources.add(createResource("mr", "soil_health", "मिटतीतील जैविक पदार्थ वाढविण्याचे उपाय",
                                "कंपोस्ट, हिरवा खेत, शेत अवशेषे, व कमी जुळवणी द्वारया उर्वरता वाढविणे.",
                                null));
                resources.add(createResource("mr", "weather", "अप्रत्याशित फ्रॉस्ट पासून पिकांची सुरक्षा",
                                "मुल्चिंग, विंडब्रेक, व पाणी देण्याचा timing जोडून फ्रॉस्ट neutralizing करणे.",
                                null));
                resources.add(createResource("mr", "market_storage",
                                "धान्याखालील हानि टाळण्यासाठी सर्वोत्तम भंडारण पद्धती",
                                "शुध वाहक, सुखविणे, एअरटाइट कंटेनर, नियमित अंदाज यांच्या द्वारा कवक व कीट टाळणे.",
                                null));

                educationResourceRepository.saveAll(resources);
        }

        private EducationResource createResource(String language, String category, String title, String content,
                        String externalUrl) {
                EducationResource resource = new EducationResource();
                resource.setLanguage(language);
                resource.setCategory(category);
                resource.setTitle(title);
                resource.setContent(content);
                resource.setExternalUrl(externalUrl);
                return resource;
        }
}