package com.example.ligtastanim;

import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import com.example.ligtastanim.ml.PestDetection2;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.annotation.Nullable;

public class Detection2 extends AppCompatActivity {

    TextView result, demoTxt, recommendation, diagnosis, tipstext, diaa, recomm, tips;
    LinearLayout linearLayout, linearLayout2, linearLayout3;
    ImageView imageView, dia, recom, tip;
    Button picture;
    int imageSize = 224;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detection);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        result = findViewById(R.id.result);
        diagnosis= findViewById(R.id.diagnosis);
        recommendation = findViewById(R.id.recommendationsText);
        tipstext = findViewById(R.id.tipsText);
        imageView = findViewById(R.id.imageView);
        picture = findViewById(R.id.button);
        demoTxt = findViewById(R.id.demoText);
        diagnosis = findViewById(R.id.diagnosis);
        diaa = findViewById(R.id.diaa);
        recomm = findViewById(R.id.recomm);
        tips = findViewById(R.id.tips);
        linearLayout = findViewById(R.id.linearLayout);
        linearLayout2 = findViewById(R.id.linearLayout2);
        linearLayout3 = findViewById(R.id.linearLayout3);
        dia = findViewById(R.id.dia);
        recom = findViewById(R.id.recom);
        tip = findViewById(R.id.tip);

        demoTxt.setVisibility(View.VISIBLE);
        result.setVisibility(View.GONE);
        recommendation.setVisibility(View.GONE);
        diagnosis.setVisibility(View.GONE);
        tipstext.setVisibility(View.GONE);
        diaa .setVisibility(View.GONE);
        recomm.setVisibility(View.GONE);
        tips.setVisibility(View.GONE);
        linearLayout.setVisibility(View.GONE);
        linearLayout2 .setVisibility(View.GONE);
        linearLayout3.setVisibility(View.GONE);
        dia.setVisibility(View.GONE);
        recom.setVisibility(View.GONE);
        tip.setVisibility(View.GONE);

        Glide.with(this)
                .asGif()
                .load(R.drawable.scan)
                .into(imageView);

        picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, 1);
                } else {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        if (requestCode == 1 && resultCode == RESULT_OK){
            Bitmap image = (Bitmap) data.getExtras().get("data");
            int dimension = Math.min(image.getWidth(), image.getHeight());
            image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);
            imageView.setImageBitmap(image);

            demoTxt.setVisibility(View.GONE);
            result.setVisibility(View.VISIBLE);
            recommendation.setVisibility(View.VISIBLE);
            diagnosis.setVisibility(View.VISIBLE);
            tipstext.setVisibility(View.VISIBLE);
            diaa .setVisibility(View.VISIBLE);
            recomm.setVisibility(View.VISIBLE);
            tips.setVisibility(View.VISIBLE);
            linearLayout.setVisibility(View.VISIBLE);
            linearLayout2 .setVisibility(View.VISIBLE);
            linearLayout3.setVisibility(View.VISIBLE);
            dia.setVisibility(View.VISIBLE);
            recom.setVisibility(View.VISIBLE);
            tip.setVisibility(View.VISIBLE);

            image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
            classifyImage(image);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void classifyImage(Bitmap image) {
        try {
            PestDetection2 model = PestDetection2.newInstance(getApplicationContext());

            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            int[] intValue = new int[imageSize * imageSize];
            image.getPixels(intValue, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());

            int pixel = 0;
            for (int i = 0; i < imageSize; i++) {
                for (int j = 0; j < imageSize; j++) {
                    int val = intValue[pixel++];
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255.f));
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255.f));
                    byteBuffer.putFloat((val & 0xFF) * (1.f / 255.f));
                }
            }
            inputFeature0.loadBuffer(byteBuffer);

            PestDetection2.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeatures0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidence = outputFeatures0.getFloatArray();

            int maxPos = 0;
            float maxConfidence = 0;
            for (int i = 0; i < confidence.length; i++){
                if (confidence[i] > maxConfidence) {
                    maxConfidence = confidence[i];
                    maxPos = i;
                }
            }

            float threshold = 0.8f;
            String[] classes = {"Bacterial blight", "Blast", "Brownspot", "grasshoper", "Leafsmut",
                    "Tungro", "White Grub", "Wire Worm", "Unknown"};

            String[] diagnosiss = {
                    "Sintomas: Ang bacterial blight ay nagdudulot ng madilaw at mamantsang mga dahon na kalaunan ay natutuyo. Sa simula, makikita ang mga maliliit na puti o dayami na batik na unti-unting lumalawak at bumubuo ng mga pahalang na guhit sa mga dahon. Kapag lumala, ang mga dulo ng dahon ay natutuyo.\n" +
                            "Panahon: Karaniwan itong mas aktibo sa mga panahong mainit at basa (tulad ng tag-ulan).\n" +
                            "Pagkakalat: Ang sakit ay maaaring kumalat sa pamamagitan ng hangin, ulan, tubig, at mga kontaminadong kagamitan.\n",
                    "Ang blast ay isang karaniwang sakit sa palay na sanhi ng fungus na Magnaporthe oryzae. Narito ang mga pangunahing impormasyon tungkol sa diagnosis, pamamahala, at tips para mapigilan ang sakit na ito:\n" +
                            "•\tSintomas: Ang blast ay makikilala sa pagkakaroon ng mga maliit na bilog o hugis-diyamanteng batik sa mga dahon. Sa umpisa, ang mga batik na ito ay kulay abo o berde at may kayumangging gilid. Habang lumalala ang sakit, ang mga batik ay lumalaki at nagiging sanhi ng pagkatuyo ng buong dahon. Ang blast ay maaari ding makaapekto sa leeg ng palay (neck blast), na nagiging sanhi ng pamumutol ng mga tangkay at pagkakaroon ng ‘blighted panicles’.\n" +
                            "•\tPanahon: Mas aktibo ang blast kapag ang klima ay malamig at mahalumigmig. Karaniwang mas mataas ang insidente nito sa mga lugar na may mas malamig na gabi, lalo na kung maulan.\n" +
                            "•\tPagkakalat: Ang spores ng fungus ay maaaring kumalat sa pamamagitan ng hangin, ulan, at kontaminadong tubig.\n",
                    "Ang brown spot ay isang sakit sa palay na sanhi ng fungus na Cochliobolus miyabeanus (kilala rin bilang Helminthosporium oryzae). Ito ay maaaring magdulot ng malubhang pinsala, lalo na sa mahihirap na kondisyon tulad ng kakulangan sa nutrisyon at tubig. Narito ang mga paraan upang masuri, mapamahalaan, at mapigilan ang brown spot sa palay:\n" +
                            " Diagnosis ng Brown Spot\n" +
                            "•\tSintomas: Ang brown spot ay nagpapakita ng maliit, bilog o hugis-itlog na mga batik sa mga dahon. Sa umpisa, ang mga batik ay kulay-kayumanggi hanggang madilim na kayumanggi, at may maputing gitna. Habang tumatagal, ang mga batik ay maaaring lumaki at magdulot ng pagkatuyo ng dahon, na nagpapahina sa halaman. Ang brown spot ay maaari ring makita sa butil, na nagreresulta sa mga mapuputi at walang laman na palay.\n" +
                            "•\tPagkakalat: Kumakalat ito sa pamamagitan ng hangin, ulan, at kontaminadong binhi.\n",
                    "Ang grasshopper o tipaklong ay isang peste sa palayan na maaaring magdulot ng matinding pinsala, lalo na kung maraming tipaklong ang naroon sa isang taniman. Kumakain sila ng dahon at tangkay ng palay, na nagreresulta sa kakulangan ng photosynthesis at pagbawas ng ani. Narito ang ilang mga hakbang upang makilala, mapamahalaan, at maiwasan ang pag-atake ng mga grasshopper sa palayan:\n" +
                            "1. Pagkilala sa Peste\n" +
                            "•\tSintomas ng Pinsala: Ang grasshopper ay kumakain ng mga dahon, iniwan ang mga ito na may kagat o butas. Maaaring mabilis na ubusin ng mga tipaklong ang mga dahon at minsan ay buong halaman, lalo na kapag mataas ang populasyon nito.\n" +
                            "•\tHabitat: Karaniwang nagpapalipat-lipat ang mga ito mula sa mga damuhan patungo sa palayan. Lumalakas ang pag-atake ng grasshopper sa mga tuyong panahon dahil naghahanap sila ng mas luntiang pagkain.\n" +
                            "•\tPagdami: Ang grasshopper ay mabilis dumami sa mga lugar na may limitadong patubig at matataas na damo, na nagiging lugar ng kanilang pagpapalahi.\n",
                    "Ang leaf smut ay isang sakit sa palay na sanhi ng fungus na Entyloma oryzae. Bagaman hindi ito karaniwang nagdudulot ng malawakang pinsala, maaari itong magdulot ng pagbawas sa kalidad ng ani. Narito ang mga gabay sa pag-diagnose, pamamahala, at pag-iwas sa leaf smut sa palayan:\n" +
                            "Diagnosis\n" +
                            "•\tSintomas: Ang leaf smut ay nagdudulot ng maliliit na kulay-abong itim o kayumangging batik sa mga dahon ng palay. Karaniwang lumilitaw ang mga batik na ito na may itim na sentro o tuldok at mukhang parang maliit na \"blisters\" sa ibabaw ng dahon. Habang tumatagal, maaaring matuyo at maging marupok ang mga dahon, na nagpapababa sa kakayahan ng halaman na magsagawa ng photosynthesis.\n" +
                            "•\tPagkakalat: Kumakalat ang fungus sa pamamagitan ng hangin, ulan, at kontaminadong tubig. Madalas itong lumalabas sa mga panahong may mataas na halumigmig at temperatura, lalo na kapag maulan o basa ang lupa.\n",
                    "Ang tungro ay isa sa mga pinaka-mapaminsalang sakit sa palay sa Asya, na sanhi ng Rice Tungro Bacilliform Virus (RTBV) at Rice Tungro Spherical Virus (RTSV). Ang sakit na ito ay pangunahing naililipat ng mga berdeng leafhopper (Nephotettix virescens) sa pamamagitan ng kanilang pag-sipsip ng dagta ng palay. Ang tungro ay mabilis kumalat at maaaring magdulot ng malaking pagbaba sa ani kung hindi agad napigilan.\n" +
                            "Diagnosis\n" +
                            "•\tSintomas: Ang mga halaman na apektado ng tungro ay nagpapakita ng dilaw na kulay sa mga dahon, na nag-uumpisa sa mga mas batang dahon at lumalawak sa buong halaman. Ang mga dahon ay unti-unting nagiging kahel o dilaw-kayumanggi. Ang mga apektadong halaman ay mababa ang pagtubo, nagiging bansot, at ang mga butil na naaani ay hindi buo at may mababang kalidad.\n" +
                            "•\tPagkakalat: Naililipat ito ng berdeng leafhopper. Kapag ang isang leafhopper ay kumagat sa infected na halaman, makukuha nito ang virus at maaaring ikalat ito sa iba pang mga halaman sa palayan.\n",
                    "Ang white grub ay isang uri ng peste na kumakain ng mga ugat ng palay at iba pang pananim. Ito ay isang larva ng mga beetles, partikular ng mga uri ng Phyllophaga at Anomala. Ang mga white grub ay kilala sa pagpapalaganap ng pinsala sa mga tanim, kabilang ang palay, dahil sa kanilang pagkain sa mga ugat na nagiging sanhi ng pagkamatay ng halaman o pagbagsak ng ani.\n" +
                            "Diagnosis\n" +
                            "Ang mga apektadong palay ay may mga dahon na nagsisimulang magkulang sa sustansya dahil sa pagka-apekto ng mga ugat. Karaniwan, ang mga dahon ay nagiging dilaw at natutuyo.\n" +
                            "Sa matinding kaso, ang mga halaman ay maaaring matumba dahil sa pagkasira ng ugat na nagiging sanhi ng kawalan ng kakayahan ng mga tanim na kumuha ng tubig at nutrients.\n" +
                            "Kung titingnan ang lupa sa paligid ng mga halaman, makikita ang mga galos at pagsira sa mga ugat, pati na rin ang mga larva ng white grub sa ilalim ng lupa.\n" +
                            "Pagkakalat: Ang mga white grub ay kumakalat sa pamamagitan ng mga itlog na inilalagay ng mga adult beetles sa lupa. Ang mga larvae ay kumakain ng mga ugat ng mga tanim at nagiging sanhi ng pinsala habang patuloy na lumalaki.\n",
                    "Ang wireworm ay isang uri ng peste na ang larvae ay nagdudulot ng pinsala sa mga ugat ng palay at iba pang mga pananim. Ang wireworm ay isang larva ng mga beetle mula sa pamilya Elateridae, na kilala sa kanilang matigas at mahahabang katawan na may kulay kahel hanggang dilaw. Ang mga wireworms ay karaniwang nagdudulot ng pinsala sa pamamagitan ng kanilang pagkain ng mga ugat, na nagiging sanhi ng kahinaan ng halaman at pagka-bansot nito.\n" +
                            "Diagnosis\n" +
                            "Ang mga halaman na apektado ng wireworm ay karaniwang may mga dahon na nagiging dilaw, natutuyo, at humihina.\n" +
                            "Ang mga apektadong halaman ay maaaring magkaroon ng mga ugat na sira o naputol, at madalas ay matutuklasan ang mga larvae ng wireworm sa paligid ng mga ugat na ito.\n" +
                            "Kapag ang infestation ay malala, ang mga halaman ay maaaring tumumba o mamatay dahil sa pagkasira ng ugat at hindi pagkuha ng sustansya mula sa lupa.\n" +
                            "Pagkakalat: Ang wireworms ay kumakalat sa pamamagitan ng mga itlog na inilalagay ng mga adult beetle sa lupa. Ang mga larvae ay dumadaan sa iba't ibang yugto ng pag-unlad, mula sa itlog hanggang sa larva, na kumakain sa mga ugat ng mga halaman habang lumalaki.\n",
                    "",

            };

            String[] recommendations = {
                    "Paggamit ng Likas na Matibay na Barayti: Pumili ng mga barayti na may resistensya sa bacterial blight. Sa Pilipinas, may mga barayti na tulad ng PSB Rc82 (Peñaranda), Rc222 (Tubigan 18), at iba pa na mas matibay laban sa sakit.\n" +
                            "Pagsunod sa Wastong Pagpapatubig: Iwasan ang sobrang tubig sa palayan, lalo na kung ang palay ay nasa murang yugto. Ang tamang patubig ay makatutulong upang hindi kumalat ang bakterya.\n" +
                            "Pagkontrol ng Mga Damo: Ang mga damo ay maaaring maging tagapagdala ng sakit, kaya’tmahalaga na alisin ang mga ito sa paligid ng palayan.\n" +
                            "Pag-iwas sa Sobrang Paggamit ng Nitrogen: Ang labis na pataba na nitrogen ay nagiging sanhi ng paglambot ng mga dahon na madaling dapuan ng sakit. Sundin ang tamang dami ng nitrogen fertilizer para sa mas malusog na halaman.\n",
                    "•\tPaggamit ng Resistant Varieties: Pumili ng mga barayti na mas matibay laban sa blast, tulad ng NSIC Rc192, Rc238, at iba pang mga barayting inirerekomenda sa lokalidad. Ang mga resistant varieties ay malaki ang naitutulong upang mapababa ang panganib ng sakit.\n" +
                            "•\tPag-aayos ng Patubig: Ang blast ay mas madalas lumitaw sa mga palayang may kawalan o hindi tamang pamamahala ng tubig. Panatilihin ang sapat na tubig (1-2 cm) sa palayan lalo na sa mga kritikal na yugto ng paglago ng halaman, upang maiwasan ang pagsibol ng fungus.\n" +
                            "•\tIwasan ang Sobrang Nitrogen: Ang sobrang nitrogen fertilizer ay nagpapahina sa halaman at nagiging mas sensitibo ito sa blast. Gumamit ng naaangkop na dami ng nitrogen base sa rekomendasyon para sa iyong barayti at lupa.\n" +
                            " Ligtas na Paggamit ng Fungicides\n" +
                            "•\tKung kinakailangan, gumamit ng mga fungicide na inirerekomenda laban sa blast tulad ng tricyclazole, isoprothiolane, o iba pang mga aprubadong fungicides. Siguraduhing sumunod sa tamang dosage at schedule ng aplikasyon. Karaniwan itong inirerekomendang gamitin sa simula ng paglabas ng palay o kapag lumitaw na ang mga sintomas ng sakit.\n",
                    "•\tPaggamit ng Likas na Matibay na Barayti: Piliin ang mga barayti na mas matibay laban sa brown spot, tulad ng NSIC Rc160 at Rc222. Ang mga barayting ito ay mas may resistensya at tumutulong sa pagpigil sa sakit.\n" +
                            "•\tPagsisigurado ng Tamang Nutrisyon: Ang brown spot ay madalas na lumalabas kapag kulang ang palay sa potash (potassium) at silicon. Maglagay ng tamang dami ng pataba na may potassium upang mapatibay ang halaman. Ang paggamit ng balanced fertilizer, kabilang ang nitrogen, phosphorus, at potassium, ay makakatulong upang mapalakas ang resistensya ng halaman laban sa sakit.\n" +
                            "•\tPagsunod sa Wastong Patubig: Ang tamang irigasyon ay mahalaga. Iwasan ang sobrang pagkabasa ng lupa dahil nakakatulong ito sa paglaganap ng sakit. Tiyaking sapat ang tubig, lalo na sa mga panahong mababa ang ulan.\n" +
                            "3. Ligtas na Paggamit ng Fungicides\n" +
                            "•\tKung kinakailangan, maaaring gumamit ng fungicides na inirerekomenda laban sa brown spot, tulad ng propiconazole at mancozeb. Mag-spray ng fungicides sa maagang yugto ng pamumuo ng mga batik upang mapigilan ang pagkalat ng fungus. Siguraduhing sumunod sa tamang dosage at frequency ng aplikasyon ng fungicide.\n" +
                            "4. Sanitation at Pag-iwas sa Pagkalat ng Sakit\n" +
                            "•\tPaglilinis ng mga Kagamitan: Siguraduhing malinis ang mga kagamitan bago at pagkatapos gamitin upang hindi maikalat ang fungus.\n" +
                            "•\tPaggamit ng Malusog na Binhi: Gumamit ng malinis at certified seeds na walang kontaminasyon. Kung hindi available ang certified seeds, maaaring isailalim ang mga binhi sa seed treatment bago itanim.\n" +
                            "5. Pagmo-monitor at Pagtutok sa Palayan\n" +
                            "•\tRegular na Pag-inspeksyon: Regular na tingnan ang iyong palayan, lalo na kapag ang kondisyon ng panahon ay mainit at basa. Agad na itala ang mga sintomas ng sakit at kumilos batay sa mga hakbang na inirerekomenda.\n" +
                            "•\tPakikipag-ugnayan sa Agricultural Experts: Kumonsulta sa mga lokal na agrikulturist para sa tamang mga pamamaraan at patuloy na edukasyon sa pamamahala ng brown spot.\n",
                    "•\tPag-aalis ng Damo: Ang mga damo ay nagsisilbing tahanan at pinagmumulan ng pagkain ng mga grasshopper, kaya’t mahalaga na alisin ang mga ito sa paligid ng palayan.\n" +
                            "•\tPagpapanatili ng Tamang Patubig: Ang regular na pagpapanatili ng tamang antas ng tubig sa palayan ay makakatulong sa pagpigil sa pagdami ng mga tipaklong. Mas mahirap sa kanila na manatili sa mga basang lupa.\n" +
                            "•\tPagpapayaman ng Likas na Kaaway ng Grasshopper: Hikayatin ang presensya ng mga likas na predator tulad ng mga ibon, palaka, gagamba, at iba pang insekto na kumakain ng mga grasshopper.\n",
                    "•\tPaggamit ng Mga Resistant Varieties: Pumili ng mga barayti ng palay na may resistensya laban sa fungal diseases upang mabawasan ang panganib ng leaf smut.\n" +
                            "•\tWastong Pagpapataba: Iwasan ang sobrang nitrogen fertilizer dahil nagpapahina ito sa resistensya ng palay laban sa fungal infections tulad ng leaf smut. Siguraduhing may balanseng dami ng nitrogen, phosphorus, at potassium sa pataba.\n" +
                            "•\tPagpapanatili ng Tamang Pag-aerate at Pagdidilig: Tiyaking maayos ang daloy ng hangin sa pagitan ng mga tanim upang maiwasan ang pag-iipon ng halumigmig. Kung posibleng, iwasan ang sobrang patubig dahil pabor ito sa pagdami ng fungus.\n",
                    "•\tPaggamit ng Resistant Varieties: Pumili ng mga barayti ng palay na may resistensya laban sa tungro at mga leafhopper, tulad ng NSIC Rc240 at PSB Rc14, upang mabawasan ang panganib ng pagkalat ng sakit.\n" +
                            "•\tKontrol sa Leafhopper: Maaaring gumamit ng light traps para mahuli ang mga leafhopper sa gabi, o gumamit ng pheromone traps upang makontrol ang kanilang populasyon.\n" +
                            "•\tPag-aalis ng Apektadong Halaman: Agad na alisin at sirain ang mga halaman na nagpapakita ng sintomas ng tungro upang maiwasan ang pagkalat nito sa ibang bahagi ng taniman.\n" +
                            "•\tPag-spray ng Insecticides Kung Kinakailangan: Sa mga seryosong kaso, maaaring gumamit ng insecticides laban sa mga leafhopper. Mag-spray lamang ayon sa rekomendasyon ng mga agricultural expert at tiyakin ang tamang dosis upang hindi masira ang kapaligiran at mapanatili ang mga likas na predator ng peste.\n",
                    "•\tPaggamit ng Pesticides: Ang mga chemical insecticides tulad ng carbaryl at chlorpyrifos ay epektibo laban sa white grubs. Mag-spray ng insecticides sa lupa bago o habang nagsisimula pa lang ang infestation upang maiwasan ang malalang pinsala. Siguraduhing sundin ang mga tamang dosis at gabay sa paggamit ng pesticides upang hindi makapinsala sa kapaligiran at mga hindi target na organismo.\n" +
                            "•\tPag-aalis ng Apektadong Lupa: Kung may mga aktibong infestation sa isang bahagi ng palayan, maaaring ilipat ang mga tanim sa ibang lugar at iwasan ang pagtanim sa lugar na iyon hanggang sa masugpo ang peste.\n" +
                            "•\tPagpapataba at Pag-aalaga sa Lupa: Ang tamang pagpapataba at pangangalaga sa lupa ay makakatulong upang mapataas ang resistensya ng mga halaman laban sa pests. Ang paggamit ng organikong pataba at compost ay makakatulong na mapalakas ang kalusugan ng mga ugat ng palay.\n",
                    "•\tPaggamit ng Pesticides: Ang mga insecticides tulad ng carbofuran at chlorpyrifos ay epektibo laban sa mga wireworm larvae. Mag-spray ng insecticides bago o habang nagsisimula pa lang ang infestation upang maiwasan ang malalang pinsala. Siguraduhing sundin ang mga tamang dosis at gabay sa paggamit ng pesticides upang hindi makapinsala sa kapaligiran at mga hindi target na organismo.\n" +
                            "•\tPag-aalis ng Apektadong Lupa: Kung may mga aktibong infestation sa isang bahagi ng palayan, maaaring ilipat ang mga tanim sa ibang lugar at iwasan ang pagtanim sa lugar na iyon hanggang sa masugpo ang peste.\n" +
                            "•\tPag-ikot ng Pananim: Ang pag-iwas sa pagtatanim ng parehong uri ng pananim sa parehong lugar bawat taon ay makakatulong na mabawasan ang populasyon ng wireworms sa lupa. Ipinapayo ang crop rotation para sa epektibong kontrol.\n",
                    "",

            };

            String[] tipstextt = {
                    "Sa karaniwang kasanayan, ang paggamit ng mga kemikal laban sa bacterial blight ay hindi nirerekomenda dahil ang sanhi ng sakit ay bakterya at hindi fungi. Sa halip, ang mga biopesticide o bacterial antagonists ay maaaring makatulong sa pagkontrol sa sakit, kung ito ay inaprubahan at ligtas gamitin.\n" +
                            "Paglilinis ng mga Kagamitan: Siguraduhing malinis ang mga kagamitan sa palayan bago at pagkatapos gamitin upang maiwasan ang pagkalat ng bakterya sa ibang lugar.\n" +
                            "Pagsusunog ng Residual Plants: Ang mga natitirang halaman o mga kontaminadong bahagi ng halaman ay dapat sunugin o ibaon sa lupa para hindi maging pinagmumulan ng sakit.\n" +
                            "Pagmo-monitor: Regular na subaybayan ang iyong palayan upang maagang matukoy ang sintomas ng bacterial blight.\n" +
                            "Pakikipag-ugnayan sa Agricultural Experts: Makipagtulungan sa mga agricultural extension workers para sa pinakabagong mga pamamaraan sa pagpigil ng sakit.\n",
                    "Paglilinis ng Mga Kagamitan: Siguraduhing malinis ang mga kagamitan bago gamitin sa palayan para maiwasan ang pagkalat ng fungus.\n" +
                            "•\tPag-aalis ng Mga Residual Plants: Sunugin o ibaon ang mga tirang halaman sa pagtatapos ng bawat ani para maiwasan ang pagkalat ng spores ng blast sa susunod na taniman.\n" +
                            "Regular Monitoring at Documentation\n" +
                            "•\tPagmo-monitor ng Halaman: Regular na subaybayan ang mga palayan, lalo na kapag malamig at basa ang panahon. Agad na magtala ng anumang sintomas at isagawa ang mga rekomendadong hakbang kung kinakailangan.\n" +
                            "•\tPakikipag-ugnayan sa Agricultural Experts: Makipagtulungan sa mga lokal na agrikulturist para sa tamang pamamahala at upang makasabay sa mga bagong rekomendasyon para sa pag-iwas sa blast.\n",
                    "•\tIwasan ang sobrang paglalagay ng nitrogen dahil maaaring mas palalain nito ang sakit.\n" +
                            "•\tTiyakin ang wastong tamang distansya ng tanim para sa mas maayos na daloy ng hangin at upang maiwasan ang pagkabasa ng mga dahon.\n",
                    " Ligtas na Paggamit ng Pesticides\n" +
                            "•\tSpot Treatment: Sa halip na ispray sa buong palayan, maaaring mag-apply ng insecticides sa mga lugar kung saan matindi ang pagdami ng grasshopper. Gumamit ng mga selective pesticides na inirerekomenda para sa mga grasshopper upang mapanatiling ligtas ang mga likas na kaaway.\n" +
                            "•\tOrganic Pesticides: Kung maaari, gumamit ng organic o likas na insecticides tulad ng neem extract o sabaw ng bawang na maaaring makatulong sa pagpigil sa peste na hindi nakakapinsala sa kapaligiran.\n" +
                            " Mga Karagdagang Tips sa Pag-iwas\n" +
                            "•\tPagsusunog ng mga Residual Plants: Sa pagtatapos ng bawat ani, sunugin o ibaon ang mga natitirang halaman upang mabawasan ang lugar na pinamumugaran ng grasshopper.\n" +
                            "•\tPaggamit ng Light Traps: Maaaring gumamit ng light traps sa gabi upang mahuli ang mga adult grasshopper na aktibo sa gabi.\n" +
                            "•\tPag-iwas sa Overgrazing ng Mga Hayop: Ang labis na pagpapakain ng mga hayop sa mga damuhan malapit sa palayan ay maaaring magdulot ng paglipat ng mga tipaklong sa palayan dahil nawawalan sila ng natural na pagkain.\n" +
                            " Regular na Pag-inspeksyon\n" +
                            "•\tMonitoring ng Taniman: Regular na pag-inspeksyon sa palayan ay mahalaga upang agad na makita ang presensya ng mga grasshopper at kumilos kaagad bago pa lumala ang sitwasyon.\n" +
                            "•\tPakikipag-ugnayan sa Agricultural Experts: Kumonsulta sa mga eksperto upang matukoy ang tamang pamamahala sa populasyon ng grasshopper sa iyong palayan at kung kinakailangan, upang makakuha ng mga payo sa paggamit ng mas makabagong pest control techniques.\n",
                    "•\tPag-aalis ng Apektadong Halaman: Kung may mga apektadong halaman, alisin at ihiwalay ang mga ito upang maiwasan ang pagkalat ng spores.\n" +
                            "•\tPaglilinis ng Kagamitan: Siguraduhing malinis ang mga kagamitan sa pagsasaka bago at pagkatapos gamitin upang maiwasan ang kontaminasyon ng mga fungal spores.\n" +
                            "•\tPagkontrol ng Damo: Ang mga damo ay maaaring maging lugar na pamahayan ng mga fungal spores, kaya't iwasang magkaroon ng labis na damo sa paligid ng palayan.\n" +
                            "•\tSpot Treatment: Sa mga matitinding kaso, maaaring gumamit ng mga fungicides na inirerekomenda para sa leaf smut. Tiyakin ang tamang dosis at frequency ng paggamit. Makipag-ugnayan sa mga agricultural experts para sa tamang uri ng fungicide na maaaring gamitin.\n" +
                            "•\tMaagang Pag-detect: Regular na inspeksyon ng palayan upang matukoy agad ang mga sintomas ng leaf smut. Agad na magsagawa ng mga hakbang kung makikita ang mga unang sintomas upang hindi ito lumala.\n",
                    "•\tPagpapanatili ng Kalinisan sa Paligid ng Palayan: Tiyaking walang damo o iba pang halaman sa paligid ng palayan dahil ang mga ito ay maaaring maging tahanan ng mga leafhopper.\n" +
                            "•\tPagbabad ng Binhi sa Mainit na Tubig: Ang pagbabad ng mga binhi sa mainit na tubig bago itanim ay makakatulong upang mabawasan ang pagkalat ng virus sa pamamagitan ng infected na mga binhi.\n" +
                            "•\tAng sobrang nitrogen sa lupa ay nagpapalakas ng pag-atake ng leafhoppers. Siguraduhin na balanse ang pataba upang mapanatili ang resistensya ng halaman.\n" +
                            "•\tMonitoring ng Taniman: Regular na inspeksyon ng palayan upang matukoy ang mga unang palatandaan ng tungro. Kapag nakita agad, maaari nang magsagawa ng mga hakbang upang hindi na ito kumalat nang husto.\n",
                    "•\tNatural Predators: Ang mga parasitic nematodes at iba pang mga natural na predator tulad ng mga ibon at mga insekto na kumakain ng mga larvae ng white grub ay makakatulong upang mabawasan ang populasyon ng peste.\n" +
                            "•\tBaiting: Ang paggamit ng mga bait traps na naglalaman ng attractants ay makakatulong sa pagkuha ng adult beetles na naglalagay ng mga itlog sa lupa. Maaaring magamit ang mga fermented molasses o iba pang attractants upang mahuli ang mga beetles bago pa man nila ilagay ang kanilang mga itlog.\n" +
                            "•\tPag-alis ng Residual Plants: Matapos anihin, tiyaking alisin ang mga natirang tanim at ang mga root systems na maaaring maglaman ng larvae. Magiging epektibo ito sa pag-iwas sa muling pagdami ng mga larvae sa susunod na panahon ng pagtatanim.\n" +
                            "•\tPag-aalaga sa Kalinisan ng Lupa: Iwasang magtanim sa mga lugar na may mataas na infektadong populasyon ng white grub at tiyaking may tamang pangangalaga sa lupa upang hindi maging paborable ang kondisyon para sa pesteng ito.\n" +
                            "•\tPagtatanim ng mga Alternatibong Pananim: Pagtatanim ng mga pananim na hindi paborable sa white grub sa mga lugar na madalas tamaan ng peste, upang mabawasan ang populasyon nito.\n",
                    "•\tNatural Predators: Ang mga parasito o predator na insekto tulad ng nematodes ay epektibo sa paglaban sa wireworm infestation. Ang mga parasitic nematodes ay kumakain ng larvae ng wireworm sa lupa.\n" +
                            "•\tUse of Beneficial Organisms: Ang mga ibon, gaya ng mga langaw at ibang insekto na kumakain ng mga larvae, ay makakatulong sa pagkontrol ng populasyon ng wireworm.\n" +
                            "•\tPag-aalis ng Residual Plants: Matapos anihin, tiyaking alisin ang mga natirang tanim at ang mga root systems na maaaring maglaman ng larvae ng wireworm. Magiging epektibo ito sa pag-iwas sa muling pagdami ng larvae sa susunod na panahon ng pagtatanim.\n" +
                            "•\tPag-aalaga sa Kalinisan ng Lupa: Iwasang magtanim sa mga lugar na may mataas na infektadong populasyon ng wireworm at tiyaking may tamang pangangalaga sa lupa upang hindi maging paborable ang kondisyon para sa pesteng ito.\n" +
                            "•\tInspeksyon ng Lupa: Regular na inspeksyon sa lupa upang matukoy kung may mga larvae ng wireworm. Ang pagkakita ng mga larvae sa ilalim ng lupa ay isang indikasyon na may problema, at maaari nang magsagawa ng mga hakbang upang maiwasan ang pagpaparami ng peste.\n" +
                            "•\tPagtatanim ng mga Alternatibong Pananim: Pagtatanim ng mga pananim na hindi paborable sa wireworm sa mga lugar na madalas tamaan ng peste, upang mabawasan ang populasyon nito.\n",
                    "",

            };

            if (maxConfidence < threshold) {
                result.setText("Unknown");
                recommendation.setText("No specific recommendation available.");
            } else {
                result.setText(classes[maxPos]);
                diagnosis.setText(diagnosiss[maxPos]);
                recommendation.setText(recommendations[maxPos]);
                tipstext.setText(tipstextt[maxPos]);
            }

            result.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view){
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https:www.google.com/search?g=")));
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
