package com.example.ligtastanim;

import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ProgressDialog;

import com.bumptech.glide.Glide;
import com.example.ligtastanim.ml.PestDetection;
import com.example.ligtastanim.ml.PestDetection2;
import com.example.ligtastanim.ml.PestDetection3;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.io.ByteArrayOutputStream;

import javax.annotation.Nullable;

import androidx.annotation.NonNull;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.database.ServerValue;

public class Detection extends AppCompatActivity {

    TextView result, demoTxt, recommendation, diagnosis, tipstext, diaa, recomm, tips;
    LinearLayout linearLayout, linearLayout2, linearLayout3;
    ImageView imageView, dia, recom, tip;
    Button picture, sendToDAButton;
    int imageSize = 224;

    private static final String TAG = "Detection";
    private DatabaseReference mDatabase;
    private String phoneNumber;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private Bitmap capturedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detection);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        Intent intent = getIntent();
        phoneNumber = intent.getStringExtra("phoneNumber");

        Log.d(TAG, "Received phone number: " + phoneNumber);

        if (phoneNumber == null || phoneNumber.isEmpty()) {
            Log.e(TAG, "Phone number is null or empty");
            Toast.makeText(this, "Error: User not properly identified", Toast.LENGTH_SHORT).show();
            finish();
            return;
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
        sendToDAButton = findViewById(R.id.button);

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
        sendToDAButton.setVisibility(View.GONE);

        Glide.with(this)
                .asGif()
                .load(R.drawable.scan)
                .into(imageView);

        byte[] byteArray = getIntent().getByteArrayExtra("capturedImage");

        if (byteArray != null) {
            Bitmap image = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

            if (image != null) {
                int dimension = Math.min(image.getWidth(), image.getHeight());
                image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);

                Glide.with(this).clear(imageView); 
                imageView.setImageBitmap(image); 

                demoTxt.setVisibility(View.GONE);
                result.setVisibility(View.VISIBLE);
                recommendation.setVisibility(View.VISIBLE);
                diagnosis.setVisibility(View.VISIBLE);
                tipstext.setVisibility(View.VISIBLE);
                diaa.setVisibility(View.VISIBLE);
                recomm.setVisibility(View.VISIBLE);
                tips.setVisibility(View.VISIBLE);
                linearLayout.setVisibility(View.VISIBLE);
                linearLayout2.setVisibility(View.VISIBLE);
                linearLayout3.setVisibility(View.VISIBLE);
                dia.setVisibility(View.VISIBLE);
                recom.setVisibility(View.VISIBLE);
                tip.setVisibility(View.VISIBLE);

                image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
                classifyImage(image);
            }
        }
    }

    private void classifyImage(Bitmap image) {
        try {
            capturedImage = image;

            PestDetection model1 = PestDetection.newInstance(getApplicationContext());
            TensorBuffer inputFeature1 = preprocessAndGetInputBuffer(image);
            PestDetection.Outputs outputs1 = model1.process(inputFeature1);
            float[] confidenceScores1 = outputs1.getOutputFeature0AsTensorBuffer().getFloatArray();

            PestDetection2 model2 = PestDetection2.newInstance(getApplicationContext());
            TensorBuffer inputFeature2 = preprocessAndGetInputBuffer(image);
            PestDetection2.Outputs outputs2 = model2.process(inputFeature2);
            float[] confidenceScores2 = outputs2.getOutputFeature0AsTensorBuffer().getFloatArray();

            PestDetection3 model3 = PestDetection3.newInstance(getApplicationContext());
            TensorBuffer inputFeature3 = preprocessAndGetInputBuffer(image);
            PestDetection3.Outputs outputs3 = model3.process(inputFeature3);
            float[] confidenceScores3 = outputs3.getOutputFeature0AsTensorBuffer().getFloatArray();

            int maxPos1 = getMaxConfidenceIndex(confidenceScores1);
            float maxConfidence1 = confidenceScores1[maxPos1];

            int maxPos2 = getMaxConfidenceIndex(confidenceScores2);
            float maxConfidence2 = confidenceScores2[maxPos2];

            int maxPos3 = getMaxConfidenceIndex(confidenceScores3);
            float maxConfidence3 = confidenceScores3[maxPos3];

            String[] classes1 = {"Common Rust", "Leaf Spot", "Leaf Blight", "Wire Worm", "White Grub",
                    "Flea Beetle", "Corn Borer", "Corn Aphid", "Fall Armyworm", "Streak Virus",
                    "Leaf Beetle", "Grasshopper", "Unknown"};

            String[] classes2 = {"Bacterial blight", "Blast", "Brownspot", "Grasshopper", "Leaf Smut",
                    "Tungro", "White Grub", "Wire Worm", "Unknown"};

            String[] classes3 = {"rice leaf roller", "rice leaf caterpillar", "yellow rice borer", "rice gall midge",
                    "Rice Stemfly", "brown plant hopper", "rice water weevil", "grain spreader thrips", "wheat blossom midge",
                    "mole cricket", "longlegged spider mite", "beet fly", "flax budworm", "beet weevil", "sericaorient alismots chulsky"};

            String[] classes4 = {"alfalfa weevil", "english grain aphid", "green bug", "penthaleus major ",
                    "Beet spot flies", "meadow moth", "Locustoidea", "lytta polita", "blister beetle",
                    "alfalfa seed chalcid", "Apolygus lucorum"};

            String[] diagnosiss1 = {
                    "Ang common rust ay isang sakit sa mais na sanhi ng halamang-singaw na Puccinia sorghi. Kadalasan itong nakikita sa mga dahon ng mais bilang mga maliliit na kayumangging butlig o pustules na maaaring kumalat sa buong halaman. Kapag napabayaan, pwedeng bumaba ang kalidad ng mais at maapektuhan ang ani.\n" +
                            "Mga Palatandaan ng Common Rust:\n" +
                            "•\tMga mala-kalawang na butlig sa ibabaw at ilalim ng dahon.\n" +
                            "•\tPangungupas ng kulay ng dahon at pagkalanta kapag matindi ang impeksyon.\n" +
                            "•\tMaaaring magdulot ng pagbawas sa ani at kalidad ng bunga.\n",
                    "Ang leaf spot ay isang sakit sa mga halaman na dulot ng iba't ibang uri ng fungi o bacteria. Kadalasan, nagiging sanhi ito ng pagkakaroon ng maliliit na bilog o patse sa mga dahon, na nagiging kayumanggi o itim sa gitna at may dilaw na gilid. Kapag napabayaan, ang mga patse ay lumalaki at maaaring ikahina ng halaman o magdulot ng maagang pagkahulog ng mga dahon.\n" +
                            "Mga Palatandaan ng Leaf Spot:\n" +
                            "•\tMaliliit na bilog o patse na madilim ang kulay (kayumanggi o itim) sa dahon.\n" +
                            "•\tDilaw na palibot o gilid sa mga patse.\n" +
                            "•\tUnti-unting pangungupas o pagkahulog ng mga apektadong dahon.\n",
                    "Ang leaf blight ay isang sakit sa mga halaman na karaniwang dulot ng fungi (tulad ng Exserohilum turcicum sa mais) o bacteria, at nagdudulot ito ng mabilis na pagkabulok ng mga dahon. Ang sakit na ito ay madaling makilala sa mga mahahabang patse o batik na kulay kayumanggi o itim na kumakalat mula sa gilid ng dahon patungo sa gitna. Ang mga apektadong dahon ay madalas matuyo at malanta, na nagiging sanhi ng pagbaba ng ani.\n" +
                            "Mga Palatandaan ng Leaf Blight:\n" +
                            "•\tMahahabang patse o batik sa mga dahon, na maaaring magsimula sa dilaw at maging kayumanggi o itim.\n" +
                            "•\tPagkakaranta at maagang pagkahulog ng mga dahon.\n" +
                            "•\tPagkawala ng lakas ng halaman, na nagpapababa ng ani o kalidad ng bunga.\n",
                    "Ang wireworm ay ang larvae ng mga click beetles, at madalas itong nagdudulot ng pinsala sa mga tanim gaya ng patatas, kamote, mais, at iba pang root crops. Kilala ang wireworm sa kanilang makintab at matigas na katawan na parang wire, at ito ay tumitira sa lupa kung saan kinakain nila ang ugat o buto ng halaman. Dahil dito, mahina ang halaman at maaari ring mamatay bago pa ito lumago.\tMga Palatandaan ng Peste ng Wireworm:\n" +
                            "•\tMabagal na paglago o pagkalanta ng mga bagong usbong na halaman.\n" +
                            "•\tMga butas o pinsala sa ugat at buto ng tanim.\n" +
                            "•\tBiglaang pagkamatay ng halaman, lalo na sa mga root crops tulad ng patatas.\n",
                    "Ang white grub ay isang uri ng peste sa taniman na dulot ng larvae ng mga salagubang o beetles (tulad ng June beetles at Japanese beetles). Karaniwan itong tumutubo sa lupa, kung saan kinakain nito ang mga ugat ng halaman, na nagiging sanhi ng paghina o pagkalanta ng mga halaman. Ang peste na ito ay karaniwang nakikita sa mga root crops at mga tanim na may malalalim na ugat.\n" +
                            "Mga Palatandaan ng Peste ng White Grub:\n" +
                            "•\tPangungupas o pagkakalanta ng mga halaman, lalo na sa mga mas batang tanim.\n" +
                            "•\tMahinang paglago o biglaang pagkamatay ng halaman.\n" +
                            "•\tMadaling natatanggal ang halaman sa lupa dahil sa pagkasira ng mga ugat.\n" +
                            "•\tKapag hinukay ang lupa, maaaring makita ang mga white grub na may makintab at puting katawan na may madilaw na ulo.\nations.",
                    "Ang flea beetle ay isang maliit na peste na kilala sa pagtalon mula sa isang halaman patungo sa iba kapag ito ay nagambala, tulad ng pulgas, kaya tinawag itong flea beetle. Ito ay nagdudulot ng pinsala sa pamamagitan ng pagngatngat sa mga dahon, na nag-iiwan ng maliliit na butas o butas-butas na anyo sa mga dahon. Kadalasan ay naaapektuhan nito ang mga batang halaman at seedlings, lalo na ang mga gulay gaya ng repolyo, talong, at iba pang leafy greens.\n" +
                            "Mga Palatandaan ng Peste ng Flea Beetle:\n" +
                            "•\tMaliliit na butas o butas-butas na bakas sa mga dahon.\n" +
                            "•\tPaminsan-minsan, ang mga dahon ay nagkakaroon ng yellow spots o nagiging brown dahil sa matinding pinsala.\n" +
                            "•\tMakikitang maliliit, makintab na itim o kayumangging insekto na tumatalon kapag ang halaman ay nahipo o nagambala.\n",
                    "Ang corn borer ay isang peste na kilala sa pagdudulot ng pinsala sa mais at iba pang pananim. Ang larvae ng corn borer ay nagtatago at nagpapakain sa loob ng stem o puno ng mais, na nagiging sanhi ng paghina ng halaman at pagliit ng ani. Sa pagsira ng mga stem, nagiging mas madali para sa mga halaman na mabuwal o mamatay, lalo na kapag may malakas na hangin.\n" +
                            "Mga Palatandaan ng Peste ng Corn Borer:\n" +
                            "•\tMga butas sa mga stems o trunks ng mais na gawa ng larvae na pumasok sa loob.\n" +
                            "•\tPagkakaroon ng maliliit na dumi o frass sa paligid ng stem o dahon, tanda ng pagngatngat ng larvae.\n" +
                            "•\tPaghina ng halaman at pagkakaroon ng sirang mga dahon na nagiging dilaw o kayumanggi.\n" +
                            "•\tAng halaman ay madaling bumaluktot o mabuwal.\n",
                    "Ang corn aphid ay isang uri ng maliit na insekto na sumisipsip ng sap mula sa mais. Karaniwang matatagpuan ang aphids sa ilalim ng mga dahon o sa paligid ng bulaklak at bunga ng mais. Sila ay nagpapalabas ng honeydew na nagdudulot ng sooty mold, isang amag na nagpapadilim sa mga dahon at maaaring makabawas sa photosynthesis. Kapag hindi agad napigilan, maaaring magresulta ito sa paghina ng halaman at pagliit ng ani.\n" +
                            "Mga Palatandaan ng Infestasyon ng Corn Aphid:\n" +
                            "•\tMakikitang maliliit, kulay berde o itim na insekto sa ilalim ng dahon at sa stem ng mais.\n" +
                            "•\tAng mga dahon ay may malagkit na substansya na tinatawag na honeydew, na sanhi ng sooty mold.\n" +
                            "•\tPagkakaroon ng mga dahon na kulubot o dilaw na palatandaan ng matinding pinsala.\n" +
                            "•\tAng mga batang halaman ay maaaring maghinto sa paglaki o magkulang sa sigla.\n",
                    "Ang fall armyworm (Spodoptera frugiperda) ay isang mapaminsalang peste na mabilis magparami at sumisira sa mga pananim, lalo na sa mais, palay, at iba pang leafy crops. Ang larvae ng fall armyworm ay nagdudulot ng malaking pinsala sa pamamagitan ng pagngatngat sa mga dahon, stem, at bulaklak ng halaman. Kapag hindi naagapan, mabilis itong kumalat at magdulot ng matinding pinsala sa taniman.\n" +
                            "Mga Palatandaan ng Infestasyon ng Fall Armyworm:\n" +
                            "•\tMakikitang mga butas o mga sirang bahagi sa mga dahon at stem ng halaman.\n" +
                            "•\tAng larvae o uod ng fall armyworm ay may madilim na guhit sa katawan at makintab na ulo.\n" +
                            "•\tMayroon ding marka na parang \"inverted Y\" sa ulo ng mga larvae, na natatangi sa fall armyworm.\n" +
                            "•\tPagkakaroon ng mga butas na may gulo (frass) o dumi sa paligid ng nasirang bahagi ng halaman.\n",
                    "Ang streak virus ay isang sakit na sanhi ng virus, na karaniwang nakakaapekto sa mais at iba pang pananim, lalo na sa mga lugar na may mataas na insidente ng peste. Ang streak virus ay kumakalat sa pamamagitan ng mga insekto tulad ng aphids at leafhoppers na nagdadala ng virus mula sa isang halaman patungo sa iba. Kapag ang isang halaman ay nahawaan, mabilis itong nagdudulot ng pinsala sa mga dahon at maaaring magresulta sa pagliit ng ani.\n" +
                            "Mga Palatandaan ng Streak Virus:\n" +
                            "•\tPagkakaroon ng manipis na dilaw o puting guhit (streaks) sa mga dahon ng halaman.\n" +
                            "•\tAng mga dahon ay maaaring magkulay kayumanggi o magtuyo sa kalaunan.\n" +
                            "•\tPanghihina at pagbagal ng paglaki ng halaman.\n" +
                            "•\tAng mga apektadong bahagi ay nagiging marupok, at ang buong halaman ay maaaring mamatay sa matinding kaso.\n",
                    "Ang leaf beetle ay isang uri ng insekto na kumakain ng mga dahon ng halaman, at madalas na nagdudulot ng pinsala sa mga tanim tulad ng gulay, mais, at iba pang pananim. Ang mga adult na beetle ay karaniwang may maliwanag na kulay at matigas na katawan, habang ang mga larvae ay mas maliit at madalas na nakatago sa ilalim ng mga dahon. Kapag hindi naagapan, ang leaf beetle ay maaaring makapinsala nang malubha sa ani.\n" +
                            "Mga Palatandaan ng Infestasyon ng Leaf Beetle:\n" +
                            "•\tButas-butas na mga dahon, na dulot ng pagngatngat ng mga beetle.\n" +
                            "•\tAng mga adult na beetle ay makikita sa ibabaw ng mga dahon, madalas sa ilalim ng mga dahon.\n" +
                            "•\tAng mga larvae ay maaaring makitang nagtatago sa mga dahon o sa lupa.\n" +
                            "•\tPagiging mahinang paglaki ng halaman at pagdami ng mga patay na dahon.\n",
                    "Ang grasshopper ay isang uri ng peste na kilala sa kanilang kakayahang kumain ng malalaking dami ng mga dahon at iba pang bahagi ng halaman. Sila ay may mga mahahabang binti na nagbibigay-daan sa kanila upang tumalon at kumalat nang mabilis sa mga taniman. Ang infestasyon ng grasshopper ay maaaring magdulot ng malaking pinsala sa mga pananim, lalo na sa mga gulay, mais, at iba pang agricultural crops.\n" +
                            "Mga Palatandaan ng Infestasyon ng Grasshopper:\n" +
                            "•\tMalalaki at nakikitang butas sa mga dahon at tangkay ng halaman.\n" +
                            "•\tPagkakaroon ng mga damo at halamang-kahoy na walang mga dahon dahil sa pagkain ng grasshopper.\n" +
                            "•\tMakikita ang mga adult grasshopper na tumatalon o nagtatago sa mga damo o bushes.\n" +
                            "•\tPagkakaroon ng mga patay na bahagi ng mga halaman o mga damo na tinamaan ng peste.\n",
                    "Unable to recognize the pest. No recommendation available."
            };

            String[] diagnosiss2 = {
                    "Sintomas: Ang bacterial blight ay nagdudulot ng madilaw at mamantsang mga dahon na kalaunan ay natutuyo. Sa simula, makikita ang mga maliliit na puti o dayami na batik na unti-unting lumalawak at bumubuo ng mga pahalang na guhit sa mga dahon. Kapag lumala, ang mga dulo ng dahon ay natutuyo.\n" +
                            "Panahon: Karaniwan itong mas aktibo sa mga panahong mainit at basa (tulad ng tag-ulan).\n" +
                            "Pagkakalat: Ang sakit ay maaaring kumalat sa pamamagitan ng hangin, ulan, tubig, at mga kontaminadong kagamitan.\n",
                    "Ang blast ay isang karaniwang sakit sa palay na sanhi ng fungus na Magnaporthe oryzae. Narito ang mga pangunahing impormasyon tungkol sa diagnosis, pamamahala, at tips para mapigilan ang sakit na ito:\n" +
                            "•\tSintomas: Ang blast ay makikilala sa pagkakaroon ng mga maliit na bilog o hugis-diyamanteng batik sa mga dahon. Sa umpisa, ang mga batik na ito ay kulay abo o berde at may kayumangging gilid. Habang lumalala ang sakit, ang mga batik ay lumalaki at nagiging sanhi ng pagkatuyo ng buong dahon. Ang blast ay maaari ding makaapekto sa leeg ng palay (neck blast), na nagiging sanhi ng pamumutol ng mga tangkay at pagkakaroon ng 'blighted panicles'.\n" +
                            "•\tPanahon: Mas aktibo ang blast kapag ang klima ay malamig at mahalumigmig. Karaniwang mas mataas ang insidente nito sa mga lugar na may mas malamig na gabi, lalo na kung maulan.\n" +
                            "•\tPagkakalat: Ang spores ng fungus ay maaaring kumalat sa pamamagitan ng hangin, ulan, at kontaminadong tubig.\n",
                    "Ang brown spot ay isang sakit sa palay na sanhi ng fungus na Cochliobolus miyabeanus (kilala rin bilang Helminthosporium oryzae). Ito ay maaaring magdulot ng malubhang pinsala, lalo na sa mahihirap na kondisyon tulad ng kakulangan sa nutrisyon at tubig. Narito ang mga paraan upang masuri, mapamahalaan, at mapigilan ang brown spot sa palay:\n" +
                            " Diagnosis ng Brown Spot\n" +
                            "•\tSintomas: Ang brown spot ay nagpapakita ng maliit, bilog o hugis-itlog na mga batik sa mga dahon ng palay. Karaniwang lumilitaw ang mga batik na ito na may itim na sentro o tuldok at mukhang parang maliit na \"blisters\" sa ibabaw ng dahon. Habang tumatagal, ang mga batik ay maaaring lumaki at magdulot ng pagkatuyo ng dahon, na nagpapahina sa halaman. Ang brown spot ay maaari ring makita sa butil, na nagreresulta sa mga mapuputi at walang laman na palay.\n" +
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

            String[] diagnosiss3 = {
                     "Rice Leaf Roller\nPagsusuri: Ang peste na ito ay sanhi ng pagkulot ng dahon ng palay. Nilalagyan nila ng sapot ang dahon at tinutupi ito, nagreresulta sa kawalan ng photosynthesis na kinakailangan ng halaman upang tumubo.\nRekomendasyon: Gumamit ng pestisidyo na ligtas sa kapaligiran tulad ng neem oil. Panatilihing malusog ang tanim sa pamamagitan ng tamang patubig at paggamit ng pataba. Regular na suriin ang mga tanim para sa mga sintomas ng impeksiyon.\nMga Tip: Magtanim ng iba-ibang uri ng palay upang mabawasan ang panganib ng pag-atake. Ang pagpapanatili ng natural na kaaway tulad ng mga gagamba at mga ibon ay makakatulong upang kontrolin ang populasyon ng peste.",
                "Rice Leaf Caterpillar\nPagsusuri: Ang mga uod na ito ay mabilis na sumisira sa mga dahon ng palay sa pamamagitan ng pagkain nito, na nagiging sanhi ng pagbagsak ng ani.\nRekomendasyon: Gamitin ang hand-picking method kung maliit pa ang sakahan upang alisin ang mga uod. Maaari ring gumamit ng biological control tulad ng Bacillus thuringiensis (Bt).\nMga Tip: Iwasan ang sobrang paggamit ng nitrogen-based fertilizers dahil mas naaakit ang peste sa malulusog na dahon.",
                "Yellow Rice Borer\nPagsusuri: Ang peste na ito ay pumapasok sa tangkay ng palay at sinisira ang loob nito, nagreresulta sa 'deadheart' o 'whitehead' symptoms.\nRekomendasyon: Gumamit ng light traps upang hulihin ang mga adult moth. Iwasan ang monocropping at panatilihin ang tamang distansya ng pagtatanim.\nMga Tip: Panatilihing malinis ang paligid ng taniman at alisin ang mga natuyong bahagi ng tanim pagkatapos ng ani.",
                "Rice Gall Midge\nPagsusuri: Nagdudulot ito ng 'silver shoot' sa palay, kung saan hindi nagbubunga ang halaman.\nRekomendasyon: Gumamit ng resistant varieties ng palay. Regular na suriin ang tanim at alisin ang mga infected na bahagi.\nMga Tip: Gumamit ng organic farming methods upang hikayatin ang natural predators tulad ng wasps at lady beetles.",
                "Rice Stemfly\nPagsusuri: Nagiging sanhi ito ng pagkasira ng tangkay ng palay, nagreresulta sa mahinang paglaki at mababang ani.\nRekomendasyon: Gamitin ang crop rotation upang mapigilan ang pagdami ng peste. Siguraduhing maayos ang drainage sa taniman.\nMga Tip: Magtanim ng palay sa tamang panahon upang maiwasan ang peak season ng peste.",
                "Brown Plant Hopper\nPagsusuri: Sumisipsip ito ng dagta ng palay, nagdudulot ng 'hopper burn' na nagiging sanhi ng pagkalanta at pagkamatay ng tanim.\nRekomendasyon: Gumamit ng resistant varieties at mag-apply ng systemic insecticides kung kinakailangan.\nMga Tip: Iwasan ang masyadong malapit na pagtatanim at magtanim ng iba't ibang uri ng pananim upang maiwasan ang mabilis na pagkalat ng peste.",
                "Rice Water Weevil\nPagsusuri: Ang larvae nito ay sumisira sa mga ugat ng palay, na nagiging sanhi ng pagbagsak ng ani.\nRekomendasyon: Alisin ang sobrang tubig sa taniman upang mapigilan ang pagpaparami ng peste. Gumamit ng tamang pestisidyo ayon sa payo ng eksperto.\nMga Tip: Magtanim sa mas mataas na lugar upang maiwasan ang sobrang tubig na kinahihiligan ng peste.",
                "Grain Spreader Thrips\nPagsusuri: Sumisipsip ito ng katas ng bulaklak ng palay, nagiging sanhi ng mababang kalidad ng butil.\nRekomendasyon: Panatilihing malinis ang sakahan at gumamit ng insecticide kung kinakailangan.\nMga Tip: Siguraduhing maayos ang bentilasyon ng sakahan upang maiwasan ang pagdami ng peste.",
                "Wheat Blossom Midge\nPagsusuri: Sinisira nito ang butil ng trigo sa pamamagitan ng paglalagay ng itlog sa loob nito, na nagiging sanhi ng mababang kalidad ng ani.\nRekomendasyon: Gumamit ng pheromone traps upang hulihin ang mga adult midge. Gumamit ng resistant varieties ng trigo.\nMga Tip: Mag-apply ng tamang pestisidyo sa gabi kapag aktibo ang peste.",
                "Mole Cricket\nPagsusuri: Sinisira nito ang ugat ng tanim sa pamamagitan ng paghuhukay sa lupa.\nRekomendasyon: Gamitin ang mga bitag tulad ng fishmeal o tubig na may sabon upang mahuli ang peste.\nMga Tip: Magtanim ng marigold sa paligid ng sakahan dahil nakakatulong ito upang itaboy ang mole cricket.",
                "Longlegged Spider Mite\nPagsusuri: Nagdudulot ito ng mga dilaw o kayumangging spot sa dahon ng palay, na nagreresulta sa mahinang photosynthesis.\nRekomendasyon: Gumamit ng miticide na inirekomenda para sa spider mites. Panatilihing basa ang tanim upang maiwasan ang pagdami ng peste.\nMga Tip: Iwasan ang sobrang paggamit ng pestisidyo na pumapatay sa natural predators ng spider mites.",
                "Beet Fly\nPagsusuri: Ang larvae nito ay nagdudulot ng pagkatuyo ng dahon ng palay.\nRekomendasyon: Maglagay ng yellow sticky traps upang mahuli ang mga adult beet fly.\nMga Tip: Gumamit ng intercropping upang mabawasan ang panganib ng pag-atake.",
                "Flax Budworm\nPagsusuri: Sumisira ito sa mga bulaklak at butil ng tanim, nagdudulot ng mababang ani.\nRekomendasyon: Gumamit ng biological control tulad ng Trichogramma parasitoids.\nMga Tip: Regular na alisin ang mga damo na maaaring maging tirahan ng peste.",
                "Beet Weevil\nPagsusuri: Sinisira nito ang mga ugat at dahon ng tanim, na nagdudulot ng pagkaantala ng paglaki.\nRekomendasyon: Gumamit ng crop rotation at biological control upang kontrolin ang peste.\nMga Tip: Magtanim ng resistensiyang uri ng pananim upang mabawasan ang epekto ng peste.",
                "Sericaorientalismots Chulsky\nPagsusuri: Ang peste na ito ay nagiging sanhi ng pagkasira ng mga dahon ng palay sa pamamagitan ng pagkain nito.\nRekomendasyon: Gumamit ng pestisidyo na partikular na epektibo laban sa peste. Panatilihing malinis ang paligid ng taniman.\nMga Tip: Maglagay ng mga bitag sa paligid ng sakahan upang mahuli ang mga adult na peste."

            };

            String[] recommendations1 = {
                    "1.\tRegular na Pagsusuri sa Pananim\n" +
                            "•\t Magsagawa ng regular na pag-inspeksyon, lalo na tuwing maulan o basa ang panahon. Hanapin ang mga maliit na tuldok na mapula o kulay-kalawang sa dahon. Ang maagang pagtukoy sa sintomas ay makakatulong upang maiwasan ang pagkalat ng sakit.\n\n" +
                            "2. Gumamit ng Fungicides\n" +
                            "•\tKung kinakailangan, gumamit ng angkop na fungicide bilang pangontra sa rust fungus. Siguraduhing sumunod sa tamang dosage at schedule ng aplikasyon. Karaniwang ginagamit ang mga fungicide na may active ingredients gaya ng propiconazole, pyraclostrobin, o azoxystrobin.\n" +
                            "•\tPara sa karaniwang kalawang, mag-spray kapag 80% ng mga dahon na naobserbahan ay may isa o higit pang pustules. Para sa Southern rust, spray kapag 50% ng mga dahon na naobserbahan ay may isa o higit pang pustules.\n" +
                            "•\tPara sa pagiging epektibo ng fungicide, tingnan ang talahanayan ng pagiging epektibo ng fungicide para sa mais na nilikha ng Corn Disease Working Group.\n\n" +
                            "3.\tPagpapaikot-ikot ng Pananim\n" +
                            "•\tMagtanim ng ibang uri ng halaman pagkatapos ng anihan ng mais, tulad ng legumes o gulay, upang hindi makapagpalakas ng populasyon ng fungus sa lupa.\n",
                    "1.\tGawin ang Tamang Distansya sa Pagtatanim\n" +
                            "•\tIwasan ang sobrang lapit-lapit na tanim upang magkaroon ng sapat na sirkulasyon ng hangin. Ang masikip na pagtanim ay maaaring magdulot ng labis na moisture sa mga dahon, na siyang nagpapalakas ng impeksyon sa leaf spot.\n" +
                            "2.\tPag-iwas sa Sobrang Patubig\n" +
                            "•\tHuwag magbabad ng tubig sa mga tanim na mais, lalo na sa panahon ng tag-ulan, dahil ito ay maaaring magpalala ng sakit. Ang leaf spot ay mas mabilis na kumakalat sa mga basa o mahalumigmig na lugar.\n" +
                            "3.\tPagpapatanggal ng Apektadong Dahon\n" +
                            "•\tKung may mga dahon na may sintomas ng leaf spot, putulin o tanggalin agad ang mga ito upang maiwasan ang pagkalat sa ibang bahagi ng tanim.\n" +
                            "4.\tMag-rotate ng Pananim\n" +
                            "•\t Pagkatapos ng anihan, magtanim ng ibang uri ng halaman, tulad ng mga gulay o legumbre, upang mabawasan ang pagkapit ng fungus sa lupa.\n" +
                            "5.\tGumamit ng Fungicide Kung Kinakailangan\n" +
                            "•\tKung malala ang impeksyon, maaaring gumamit ng angkop na fungicide na inirerekomenda para sa leaf spot. Sumunod sa tamang dosis at schedule ng aplikasyon upang maging epektibo ito.\n" +
                            "6.\tPanatilihing Malinis ang Paligid ng Taniman\n" +
                            "•\tSiguraduhing tanggalin ang mga patay o nalaglag na dahon sa paligid ng taniman, dahil maaari itong maging pinagmumulan ng leaf spot fungus.\n",
                    "1.\tTanggalin ang mga Apektadong Dahon\n" +
                            "Putulin at itapon ang mga apektadong dahon upang maiwasan ang pagkalat ng sakit. Siguraduhing malayo ang pagtatapunan ng mga ito mula sa taniman upang hindi bumalik ang impeksyon.\n" +
                            "2.\tMag-spray ng Fungicide o Bactericide\n" +
                            "Gumamit ng naaangkop na fungicide o bactericide bases sa sanhi ng sakit (fungal o bacterial). Ang mga fungicides tulad ng mancozeb at chlorothalonil ay epektibo laban sa fungal leaf blight, habang ang mga copper-based bactericides naman ay para sa bacterial leaf blight.\n" +
                            "3.\tPagpapatuyo ng Lupa bago Muling Magdilig\n" +
                            "Iwasan ang sobrang patubig, lalo na sa panahon ng umaga, upang mabawasan ang moisture sa paligid ng halaman na nakatutulong sa pag-usbong ng mga pathogens.\n",
                    "1.\tMaghanda ng Lure Trap o Pain sa Lupa\n" +
                            "Maglagay ng piraso ng patatas, karot, o kamote sa lupa bilang pain para maakit ang mga wireworm. Balutin ito ng tela o ilagay sa isang butas sa lupa at takpan ng lupa. Pagkalipas ng 2-3 araw, alisin at itapon ang mga pain kasama ang mga wireworm.\n" +
                            "2.\tGumamit ng Organic na Pamatay Peste\n" +
                            "Maglagay ng neem oil o diatomaceous earth sa paligid ng tanim upang mabawasan ang wireworm. Ang mga natural na pest control ay nakakatulong sa pagpatay ng mga wireworm nang hindi nakakasira sa lupa.\n" +
                            "3.\t Subukan ang Pag-freeze o Solarize ng Lupa\n" +
                            "Kapag offseason, i-solarize ang lupa sa pamamagitan ng pagtakip nito ng plastic at pabayaan ito sa ilalim ng araw sa loob ng ilang linggo. Makakatulong ito upang mapatay ang mga wireworm larvae sa lupa.\n" +
                            "4.\tPaghukay at Pag-aararo ng Lupa\n" +
                            "Araruhin o bungkalin ang lupa bago magtanim upang sirain ang mga pugad ng wireworm. Ang pagbubungkal ay tumutulong upang mailantad sila sa araw at mga ibon.\n" +
                            "5.\tGumamit ng Tamang Insektisidyo Kung Kailangan\n" +
                            "Kung malala ang problema, maaaring gumamit ng insecticides na epektibo laban sa wireworm, ngunit siguraduhing sumunod sa tamang dosis at oras ng aplikasyon. Iwasang labis na paggamit ng kemikal para mapanatili ang kalusugan ng lupa.\n",
                    "1.\tMaglagay ng Organic na Pamatay-peste (Natural Pesticides)\n" +
                            "Gumamit ng neem oil o diatomaceous earth sa paligid ng taniman. Nakakatulong ang mga ito upang mapigilan ang pagkakaroon ng white grubs nang hindi nakakasira sa kapaligiran.\n" +
                            "2.\tPaglalagay ng Beneficial Nematodes\n" +
                            "Ang beneficial nematodes ay maliit na organismo na tumutulong sa pagpatay ng mga larvae ng white grub. Kapag inilagay ito sa lupa, kinakain ng nematodes ang larvae, na nakakatulong sa pagpigil sa pagdami ng mga peste.\n" +
                            "3.\tSolarization ng Lupa\n" +
                            "Sa panahon ng offseason, takpan ang lupa ng plastic at pabayaan ito sa ilalim ng araw nang 4-6 na linggo. Ang init mula sa araw ay makakatulong upang mapatay ang mga larvae ng white grub sa lupa.\n",
                    "1.\tGumamit ng Organic na Pamatay-Peste\n" +
                            "Ang neem oil o pyrethrin-based spray ay epektibo laban sa flea beetles. Mag-spray ng neem oil sa mga apektadong bahagi ng halaman upang maitaboy o mapatay ang mga flea beetles.\n" +
                            "2.\tPaglalagay ng Floating Row Covers\n" +
                            "Takpan ang mga bagong tanim na seedlings gamit ang floating row covers upang maiwasan ang pagpasok ng mga flea beetles. Siguraduhing walang butas o espasyo kung saan sila makakapasok.\n" +
                            "3.\t Maglagay ng Sticky Traps sa Taniman\n" +
                            "Ang mga sticky traps ay makakatulong sa pagkuha ng mga flea beetles. Maglagay ng mga dilaw o asul na sticky traps malapit sa mga tanim upang mahuli ang mga tumatalon na insekto.\n",
                    "1.\tMag-spray ng Biological Control tulad ng Bacillus thuringiensis (Bt)\n" +
                            "Ang Bacillus thuringiensis (Bt) ay isang natural na bakterya na nakakapinsala sa larvae ng corn borer ngunit ligtas para sa ibang mga organismo. I-spray ito sa mga apektadong bahagi ng halaman upang patayin ang mga larvae ng corn borer.\n" +
                            "2.\tTanggalin ang mga Apektadong Bahagi ng Halaman\n" +
                            "Putulin at itapon ang mga bahaging may infestasyon upang hindi na lumaganap ang mga corn borer. Siguraduhing ilayo ang mga ito sa taniman o sunugin upang hindi na bumalik ang mga peste.\n" +
                            "3.\t Gumamit ng Resistant Varieties ng Mais\n" +
                            "Pumili ng mga binhi ng mais na may resistensya laban sa corn borer. Makipag-ugnayan sa mga agricultural center para sa mga rekomendasyon sa mga resistant na varieties.\n",
                    "1.\tMag-spray ng Neem Oil o Insecticidal Soap\n" +
                            "Ang neem oil at insecticidal soap ay epektibo laban sa aphids. Mag-spray ng neem oil sa apektadong bahagi ng halaman, partikular sa ilalim ng mga dahon, upang mapatay ang mga aphid nang hindi nakakasira sa kapaligiran.\n" +
                            "2.\t Gumamit ng Ladybugs o Lacewings\n" +
                            "Ang ladybugs at lacewings ay natural na predatory insects na kumakain ng aphids. Maaari silang bilhin at ilagay sa taniman upang makatulong sa pag-kontrol ng populasyon ng aphids.\n" +
                            "3.\t Mag-spray ng Malamig na Tubig\n" +
                            "Ang pag-spray ng malamig na tubig sa mga apektadong bahagi ng halaman ay maaaring makapagpatalsik ng mga aphids mula sa mga dahon.\n",
                    "1.\tMag-spray ng Insecticide tulad ng Bacillus thuringiensis (Bt)\n" +
                            "Ang Bacillus thuringiensis (Bt) ay isang epektibong biological insecticide na nakakapinsala lamang sa larvae tulad ng fall armyworm at ligtas para sa ibang mga organismo. I-spray ito sa mga apektadong bahagi ng tanim upang patayin ang mga larvae.\n" +
                            "2.\tGumamit ng Natural na Predators\n" +
                            "Hikayatin ang mga natural na predator's ng fall armyworm tulad ng ladybugs, lacewings, at trichogramma wasps sa taniman upang makatulong sa pagkontrol ng peste.\n" +
                            "4.\tMaglagay ng Neem Oil o Biological Insecticides\n" +
                            "Ang neem oil at iba pang biological insecticides ay maaaring makatulong sa pagpatay sa larvae at pigilan ang kanilang pagdami. Mag-spray sa mga bahagi ng tanim na malapit sa mga infestasyon.\n",
                    "1.\tPagkontrol sa mga Pesteng Nagpapakalat ng Virus\n" +
                            "I-spray ng insecticide tulad ng neem oil o insecticidal soap upang mabawasan ang aphids at leafhoppers sa taniman. Ang pag-kontrol sa mga peste ay makakatulong sa pagpigil sa pagkalat ng virus.\n" +
                            "2.\t Gumamit ng Virus-Resistant Varieties\n" +
                            "Pumili ng mga binhi ng mais at iba pang pananim na may resistensya laban sa streak virus. Makipag-ugnayan sa mga lokal na agricultural center para sa mga rekomendasyon sa mga virus-resistant varieties.\n" +
                            "3.\t Itapon ang mga Apektadong Halaman\n" +
                            "Ang mga halaman na may mataas na antas ng impeksyon ay dapat tanggalin at itapon nang maayos upang hindi kumalat ang virus sa ibang mga halaman.\n",
                    "1.\tGumamit ng Insecticide\n" +
                            "Mag-spray ng insecticides tulad ng pyrethrin o neem oil sa mga apektadong bahagi ng taniman. Ang neem oil ay epektibo laban sa mga adult beetle at larvae.\n" +
                            "2.\tManual na Pag-alis ng Beetle\n" +
                            "Sa maliit na sakahan, maaari mong tanggalin ang mga beetle nang manu-mano. Magsuot ng guwantes at ilagay ang mga beetle sa isang lalagyan ng tubig na may sabon upang mamatay ang mga ito.\n" +
                            "3.\tGumamit ng Pesticidal Soaps\n" +
                            "Ang pesticidal soaps ay nakakatulong sa pagpatay ng mga leaf beetle sa pamamagitan ng pag-atake sa kanilang cuticle. I-spray ito sa mga apektadong bahagi ng halaman.\n",
                    "Gumamit ng Insecticides\n" +
                            "Mag-spray ng insecticides na may active ingredients tulad ng carbaryl o malathion upang mabawasan ang populasyon ng grasshopper. Sundin ang mga tagubilin sa label para sa tamang paggamit at dosis.\n" +
                            "2.\t Paggamit ng Natural na Pamatay-Insekto\n" +
                            "Ang neem oil o insecticidal soap ay mga ligtas at epektibong alternatibo. I-spray ito sa mga bahagi ng halaman na apektado ng grasshopper.\n" +
                            "3.\t Pag-install ng Pests Traps\n" +
                            "Maglagay ng traps sa paligid ng taniman upang mahuli ang mga adult grasshopper. Ang mga traps na gawa sa sticky materials ay makakatulong sa pag-kontrol ng populasyon.\n",
                    "Unable to recognize the pest. No recommendation available."
            };

            String[] recommendations2 = {
                    "Paggamit ng Likas na Matibay na Barayti: Pumili ng mga barayti na may resistensya sa bacterial blight. Sa Pilipinas, may mga barayti na tulad ng PSB Rc82 (Peñaranda), Rc222 (Tubigan 18), at iba pa na mas matibay laban sa sakit.\n" +
                            "Pagsunod sa Wastong Pagpapatubig: Iwasan ang sobrang tubig sa palayan, lalo na kung ang palay ay nasa murang yugto. Ang tamang patubig ay makatutulong upang hindi kumalat ang bakterya.\n" +
                            "Pagkontrol ng Mga Damo: Ang mga damo ay maaaring maging tagapagdala ng sakit, kaya'tmahalaga na alisin ang mga ito sa paligid ng palayan.\n" +
                            "Pag-iwas sa Sobrang Paggamit ng Nitrogen: Ang labis na pataba na nitrogen ay nagiging sanhi ng paglambot ng mga dahon na madaling dapuan ng sakit. Sundin ang tamang dami ng nitrogen fertilizer para sa mas malusog na halaman.\n",
                    "•\tPaggamit ng Resistant Varieties: Pumili ng mga barayti na mas matibay laban sa blast, tulad ng NSIC Rc192, Rc238, at iba pang mga barayting inirerekomenda sa lokalidad. Ang mga resistant varieties ay malaki ang naitutulong upang mapababa ang panganib ng sakit.\n" +
                            "•\tPag-aayos ng Patubig: Ang blast ay mas madalas lumitaw sa mga palayang may kawalan o hindi tamang pamamahala ng tubig. Panatilihin ang sapat na tubig (1-2 cm) sa palayan lalo na sa mga kritikal na yugto ng paglago ng halaman, upang maiwasan ang pagsibol ng fungus.\n" +
                            "•\tIwasan ang Sobrang Nitrogen: Ang sobrang nitrogen fertilizer ay nagpapahina sa halaman at nagiging mas sensitibo ito sa blast. Gumamit ng naaangkop na dami ng nitrogen base sa rekomendasyon para sa iyong barayti at lupa.\n" +
                            " Ligtas na Paggamit ng Fungicides\n" +
                            "•\tKung kinakailangan, gumamit ng mga fungicide na inirerekomenda laban sa blast tulad ng tricyclazole, isoprothiolane, o iba pang mga aprubadong fungicides. Siguraduhing sumunod sa tamang dosage at schedule ng aplikasyon. Karaniwan itong inirerekomendang gamitin sa simula ng paglabas ng palay o kapag lumitaw na ang mga sintoms ng sakit.\n",
                    "•\tPaggamit ng Likas na Matibay na Barayti: Piliin ang mga barayti na mas matibay laban sa brown spot, tulad ng NSIC Rc160 at Rc222. Ang mga barayting ito ay mas may resistensya at tumutulong sa pagpigil sa sakit.\n" +
                            "•\tPagsisigurado ng Tamang Nutrisyon: Ang brown spot ay madalas na lumalabas kapag kulang ang palay sa potash (potassium) at silicon. Maglagay ng tamang dami ng pataba na may potassium upang mapatibay ang halaman. Ang paggamit ng balanced fertilizer, kabilang ang nitrogen, phosphorus, at potassium, ay makakatulong upang mapalakas ang resistensya ng halaman laban sa sakit.\n" +
                            "•\tPagsunod sa Wastong Patubig: Ang tamang irigasyon ay mahalaga. Iwasan ang sobrang pagkabasa ng lupa dahil nakakatulong ito sa paglaganap ng sakit. Tiyaking sapat ang tubig, lalo na sa mga panahong mababa ang ulan.\n" +
                            "3. Ligtas na Paggamit ng Fungicides\n" +
                            "•\tKung kinakailangan, maaaring gumamit ng fungicides na inirerekomenda laban sa brown spot, tulad ng propiconazole at mancozeb. Mag-spray ng fungicides sa maagang yugto ng pamumuo ng mga batik upang mapigilan ang pagkalat ng fungus. Siguraduhing sumunod sa tamang dosage at frequency ng aplikasyon ng fungicide.\n" +
                            "4. Sanitation at Pag-iwas sa Pagkalat ng Sakit\n" +
                            "•\tPaglilinis ng mga Kagamitan: Siguraduhing malinis ang mga kagamitan bago at pagkatapos gamitin upang hindi maikalat ang fungus.\n" +
                            "•\tPaggamit ng Malusog na Binhi: Gumamit ng malinis at certified seeds na walang kontaminasyon. Kung hindi available ang certified seeds, maaaring isailalim ang mga binhi sa seed treatment bago itanim.\n" +
                            "5. Pagmo-monitor at Pagtutok sa Palayan\n" +
                            "•\tRegular na Pag-inspeksyon: Regular na tingnan ang iyong palayan, lalo na kapag ang kondisyon ng panahon ay mainit at basa. Agad na itala ang mga sintoms ng sakit at kumilos batay sa mga hakbang na inirerekomenda.\n" +
                            "•\tPakikipag-ugnayan sa Agricultural Experts: Kumonsulta sa mga lokal na agrikulturist para sa tamang mga pamamaraan at patuloy na edukasyon sa pamamahala ng brown spot.\n",
                    "•\tPag-aalis ng Damo: Ang mga damo ay nagsisilbing tahanan at pinagmumulan ng pagkain ng mga grasshopper, kaya't mahalaga na alisin ang mga ito sa paligid ng palayan.\n" +
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

            String[] recommendations3 = {
                "Gumamit ng pestisidyo na ligtas sa kapaligiran tulad ng neem oil. Panatilihing malusog ang tanim sa pamamagitan ng tamang patubig at paggamit ng pataba. Regular na suriin ang mga tanim para sa mga sintomas ng impeksiyon.",
                "Gamitin ang hand-picking method kung maliit pa ang sakahan upang alisin ang mga uod. Maaari ring gumamit ng biological control tulad ng Bacillus thuringiensis (Bt).",
                "Gumamit ng light traps upang hulihin ang mga adult moth. Iwasan ang monocropping at panatilihin ang tamang distansya ng pagtatanim.",
                "Gumamit ng resistant varieties ng palay. Regular na suriin ang tanim at alisin ang mga infected na bahagi.",
                "Gamitin ang crop rotation upang mapigilan ang pagdami ng peste. Siguraduhing maayos ang drainage sa taniman.",
                "Gumamit ng resistant varieties at mag-apply ng systemic insecticides kung kinakailangan.",
                "Alisin ang sobrang tubig sa taniman upang mapigilan ang pagpaparami ng peste. Gumamit ng tamang pestisidyo ayon sa payo ng eksperto.",
                "Panatilihing malinis ang sakahan at gumamit ng insecticide kung kinakailangan.",
                "Gumamit ng pheromone traps upang hulihin ang mga adult midge. Gumamit ng resistant varieties ng trigo.",
                "Gamitin ang mga bitag tulad ng fishmeal o tubig na may sabon upang mahuli ang peste.",
                "Gumamit ng miticide na inirekomenda para sa spider mites. Panatilihing basa ang tanim upang maiwasan ang pagdami ng peste.",
                "Maglagay ng yellow sticky traps upang mahuli ang mga adult beet fly.",
                "Gumamit ng biological control tulad ng Trichogramma parasitoids.",
                "Gumamit ng crop rotation at biological control upang kontrolin ang peste.",
                "Gumamit ng pestisidyo na partikular na epektibo laban sa peste. Panatilihing malinis ang paligid ng taniman."
            };

            String[] tipstextt1 = {
                    "1.\tMag-obserba ng Maigi sa Panahon ng Tag-ulan o Taglamig\n" +
                            "Mas mabilis kumalat ang common rust sa malamig at mahalumigmig na klima, kaya't bantayan ang pananim sa ganitong mga panahon.\n\n" +
                            "2.\tPanatilihing Walang Damo ang Paligid ng Maisan\n" +
                            "Ang mga damo ay maaaring magsilbing tago ng sakit kaya't ugaliin ang regular na paglilinis ng paligid ng taniman.\n\n" +
                            "3.\tPag-inspeksyon ng Regular sa Mga Pananim\n" +
                            "Suriin ang mga dahon ng mais tuwing umaga o hapon para agad makita kung may kalawang. Kung may sintomas, agad na tanggalin ang mga dahon o bahagi ng halaman na apektado.\n\n" +
                            "4.\tPagkontrol ng Patubig\n" +
                            "Huwag hayaang magbara o magkulob ang tubig sa paligid ng taniman ng mais dahil pinapaboran nito ang pag-usbong ng mga halamang-singaw.\n",
                    "1.\tPanatilihing Walang Damo ang Paligid ng Halaman\n" +
                            "Ang mga damo ay maaaring magsilbing tago o tagabuhat ng sakit kaya't mahalagang panatilihing malinis ang paligid ng taniman.\n" +
                            "2.\t Tamang Distansya ng Pagtatanim\n" +
                            "Iwasang magtanim ng masyadong dikit-dikit upang makadaloy ang hangin nang maayos sa pagitan ng mga halaman, na tumutulong sa mabilis na pagpapatuyo ng dahon at pag-iwas sa mga fungal infection.\n" +
                            "3.\tMag-Disinfect ng Gamit sa Pagtatanim\n" +
                            "Ang mga kagamitang gamit sa pagputol o pagtanim ay maaaring magdala ng sakit mula sa isang halaman patungo sa iba. Siguraduhing malinis at nadidisinfect ang mga ito bago gamitin.\n" +
                            "4.\t Iwasan ang Overhead Watering\n" +
                            "Mas mainam ang pagdidilig nang direkta sa lupa kaysa sa mga dahon, dahil ang basang dahon ay nakakapagpatibay ng kondisyon para sa fungal growth.\n",
                    "1.\tIwasan ang Overhead Watering\n" +
                            "Ugaliing magdilig nang direkta sa lupa imbes na sa dahon upang hindi mabasa ang mga dahon. Ang basang dahon ay nagiging kaaya-aya sa fungi at bacteria.\n" +
                            "2.\tPanatilihing Malinis ang Paligid ng Halaman\n" +
                            "Alisin ang mga nalaglag na dahon at damo sa paligid ng taniman, dahil ang mga ito ay maaaring maging taguan ng mga pathogens.\n" +
                            "3.\tMag-rotate ng Pananim (Crop Rotation)\n" +
                            "Iwasang magtanim ng parehong uri ng halaman sa parehong lugar taon-taon. Mag-rotate ng pananim upang mapigilan ang akumulasyon ng pathogens sa lupa.\n" +
                            "4.\t Pumili ng Resistant na Varieties\n" +
                            "Gumamit ng mga binhi na may resistensya sa leaf blight para maiwasan ang impeksyon. Makipag-ugnayan sa mga lokal na agricultural center para sa rekomendasyon ng mga binhi.\n" +
                            "5.\tMag-Disinfect ng Mga Kagamitan\n" +
                            "Linisin at i-disinfect ang mga kagamitang ginagamit sa pagtatanim upang maiwasan ang paglipat ng sakit mula sa isang halaman patungo sa iba.\n",
                    "Panatilihing Malinis ang Taniman\n" +
                            "Alisin ang mga tira-tirang tanim, ugat, at mga damo sa paligid ng taniman upang maiwasan ang pagdami ng mga wireworm. Ang mga tira-tira ay nagiging taguan ng mga peste.\n" +
                            "2.\tGamitin ang Organic na Pataba (Compost)\n" +
                            "Ang paggamit ng compost ay nakakatulong sa pagpapalakas ng halaman, kaya't mas may resistensya ito sa mga peste. Ang malusog na lupa ay hindi kaaya-aya sa mga wireworm.\n" +
                            "3.\tMag-obserba at Maghanap ng mga Wireworm Regular\n" +
                            "Tuwing pagtatanim o pag-aararo ng lupa, obserbahan kung may wireworm at alisin ang mga ito nang manu-mano kapag nakita.\n" +
                            "4.\tPiliin ang Tamang Oras ng Pagtatanim\n" +
                            "Ang wireworm ay mas aktibo sa malamig at mahalumigmig na panahon. Subukang magtanim kapag mas mainit na ang lupa para mabawasan ang posibilidad ng impeksyon.\n",
                    "1.\tPag-obserba ng Lupa bago Magtanim\n" +
                            "Bago magtanim, hukayin ang lupa sa iba't ibang bahagi ng taniman upang masuri kung may mga white grub. Kung may makita, tanggalin ang mga ito nang manu-mano.\n" +
                            "2.\tPanatilihing Walang Damo ang Paligid ng Taniman\n" +
                            "Ang mga damo at tira-tirang halaman ay maaaring maging tirahan ng mga adult beetle na nangingitlog sa lupa. Panatilihing malinis ang paligid upang mabawasan ang posibleng tirahan ng mga salagubang.\n" +
                            "3.\t Pagtanim ng mga Halamang Nagbibigay Proteksyon\n" +
                            "Ang ilang halaman, gaya ng marigold, ay may kakayahang magtaboy ng mga peste tulad ng white grub. Magtanim ng marigold sa paligid ng taniman bilang natural na panlaban sa peste.\n" +
                            "4.\tMag-rotate ng Pananim\n" +
                            "Ugaliing mag-rotate ng pananim upang maiwasan ang pagkakaroon ng akumulasyon ng white grub sa lupa. Sa ganitong paraan, mababawasan ang mga peste dahil walang patuloy na pagkain na maaaring pagkunan ng sustansya.\n" +
                            "5.\t Pagpapanatili ng Tamang Moisture ng Lupa\n" +
                            "Huwag hayaang sobrang basa o sobrang tuyo ang lupa. Ang wastong moisture ay makakatulong sa kalusugan ng halaman at maiwasan ang pag-usbong ng peste sa lupa.\n",
                    "1.\t Iwasan ang Pagtatanim ng Pare-parehong Halaman Taon-taon (Crop Rotation)\n" +
                            "Ang flea beetles ay madalas nagbabalik sa parehong uri ng halaman taon-taon. Mag-rotate ng tanim sa ibang uri upang hindi masanay ang mga peste sa lugar.\n" +
                            "2.\tPanatilihing Malusog ang mga Halaman\n" +
                            "Ang malulusog na halaman ay mas may kakayahang makabawi mula sa pinsalang dulot ng flea beetles. Gumamit ng organic na pataba tulad ng compost upang palakasin ang mga halaman at mapataas ang kanilang resistensya laban sa peste.\n" +
                            "3.\tAlisin ang mga Damong Nagiging Taguan ng Peste\n" +
                            "Ang mga damo ay maaaring maging tirahan ng flea beetles. Panatilihing malinis ang paligid ng taniman upang maiwasan ang pagkakaroon ng pugad ng mga peste.\n" +
                            "4.\tPagtanim ng Trap Crops\n" +
                            "Magtanim ng trap crops tulad ng radish sa gilid ng taniman, dahil ito ang isa sa mga paboritong tanim ng flea beetles. Kapag pinuntirya na ito ng flea beetles, madaling alisin o sirain ang trap crops kasama ng mga pesteng naninirahan doon.\n" +
                            "5.\tSubaybayan ang Taniman Regular\n" +
                            "Mag-inspeksyon ng taniman araw-araw o lingguhan upang matukoy agad kung may peste. Agad na alisin o gamutin ang mga apektadong halaman upang hindi na kumalat ang flea beetles sa iba pang mga tanim.\n",
                    "1.\tPanatilihing Malinis ang Paligid ng Taniman\n" +
                            "Ang mga natitirang tanim o debris sa paligid ng taniman ay maaaring maging pugad ng corn borer. Siguraduhing malinis ang taniman bago ang susunod na taniman ng mais.\n" +
                            "2.\tMaglagay ng Trap Crops\n" +
                            "Magtanim ng trap crops sa paligid ng taniman upang makahikayat ng corn borer sa ibang halaman imbes na sa mga pangunahing tanim. Kapag kinapitan na ng corn borer ang trap crops, madaling tanggalin ang mga apektadong tanim kasama ang mga pesteng nakatira doon.\n" +
                            "3.\tRegular na Pag-inspeksyon ng Taniman\n" +
                            "Ugaliing suriin ang taniman para agad makita kung may mga palatandaan ng corn borer. Kung maagang makikita ang infestasyon, mas madaling makontrol ang pinsala.\n" +
                            "4.\tPaggamit ng Diatomaceous Earth sa Paligid ng Taniman\n" +
                            "Ang diatomaceous earth ay isang natural na paraan upang pigilan ang mga larvae na makapasok sa mga stems. Maglagay ng manipis na layer nito sa lupa sa paligid ng halaman upang mapigilan ang pagpasok ng mga corn borer.\n",
                    "1.\tMag-inspeksyon ng Regular\n" +
                            "Regular na suriin ang mga halaman, lalo na sa ilalim ng mga dahon at sa paligid ng bulaklak, upang matukoy agad ang mga aphid bago sila dumami.\n" +
                            "2.\t Panatilihing Malusog ang mga Halaman\n" +
                            "Ang malulusog na halaman ay may mas mataas na resistensya laban sa mga peste tulad ng aphids. Tiyaking gumagamit ng organikong pataba at tamang dami ng tubig upang mapanatili silang malakas.\n" +
                            "3.\tTanggalin ang mga Damong Ligaw\n" +
                            "Ang mga damong ligaw ay maaaring magsilbing pugad ng aphids, kaya't panatilihing malinis ang paligid ng taniman upang maiwasan ang pagsanib ng mga peste.\n" +
                            "4.\tIwasan ang Labis na Paggamit ng Nitrogen Fertilizers\n" +
                            "Ang labis na nitrogen ay nagdudulot ng paglaki ng malambot na dahon na nakakaakit ng aphids. Siguraduhing balanse ang paggamit ng pataba upang hindi masyadong maging masustansya para sa mga aphids.\n" +
                            "5.\t Maglagay ng Diatomaceous Earth sa Paligid ng Halaman\n" +
                            "Ang diatomaceous earth ay makakatulong na pigilan ang aphids sa pagakyat sa mga stem at dahon ng mais. Maglagay ng manipis na layer nito sa lupa sa paligid ng taniman.\n",
                    "1.\tRegular na Pag-inspeksyon ng Taniman\n" +
                            "Maagang pag-inspeksyon ng taniman ay makakatulong upang agad na matukoy ang fall armyworm bago ito dumami. Suriin ang ilalim ng mga dahon at iba pang bahagi ng halaman para sa larvae.\n" +
                            "2.\tIwasang Magtanim ng Pare-parehong Pananim Taon-taon (Crop Rotation)\n" +
                            "I-rotate ang tanim sa iba't ibang uri ng halaman upang hindi mabuhay nang patuloy ang fall armyworm sa parehong lugar taon-taon.\n" +
                            "3.\tPanatilihing Malinis ang Paligid ng Taniman\n" +
                            "Siguraduhing walang natitirang mga damo o residue ng halaman na maaaring maging pugad ng fall armyworm. Linisin ang mga labi o residue pagkatapos ng bawat anihan.\n" +
                            "4.\tMagtanim ng Trap Crops\n" +
                            "Magtanim ng trap crops, tulad ng sorghum o millet, sa paligid ng pangunahing tanim upang ilihis ang atensyon ng fall armyworm. Kapag kinapitan na ng pesteng ito ang trap crops, maaari itong itapon o sunugin kasama ng peste.\n" +
                            "5.\tGumamit ng Diatomaceous Earth\n" +
                            "Maglagay ng diatomaceous earth sa paligid ng taniman upang hadlangan ang larvae sa pagakyat sa stem at dahon ng tanim. Ang diatomaceous earth ay isang natural na substance na epektibong nakakapatay sa larvae.\n",
                    "1.\t Regular na Pag-inspeksyon ng Taniman\n" +
                            "Suriin ang taniman nang regular upang agad na makita ang mga palatandaan ng streak virus at makapagpatupad ng maagang aksyon laban sa virus.\n" +
                            "2.\tPanatilihing Malinis ang Paligid ng Taniman\n" +
                            "Ang mga damo ay maaaring maging tirahan ng mga insekto na nagdadala ng streak virus, kaya't siguraduhing malinis ang paligid ng taniman. Alisin ang mga damo upang mabawasan ang panganib ng impeksyon.\n" +
                            "3.\tMag-Rotate ng Pananim\n" +
                            "Iwasang magtanim ng parehong uri ng pananim sa parehong lugar taon-taon upang mabawasan ang posibilidad ng paglaganap ng streak virus. Gumamit ng ibang mga pananim sa rotation upang makatulong sa pagpigil sa pagkalat ng virus.\n" +
                            "4.\tHuwag Gumamit ng Impeksyong Halaman Bilang Binhi\n" +
                            "Siguraduhing ang mga binhi ay galing sa malusog na halaman at hindi mula sa mga halaman na nagkaroon ng streak virus upang hindi ito maisalin sa susunod na pagtatanim.\n" +
                            "5.\tGumamit ng Natural na Predators ng Aphids at Leafhoppers\n" +
                            "Ang ladybugs at lacewings ay mga natural na kalaban ng aphids at leafhoppers. Hikayatin ang mga ito sa taniman bilang tulong sa pagkontrol ng mga insektong nagdadala ng virus.\n",
                    "1.\tMag-practice ng Crop Rotation\n" +
                            "Iwasang magtanim ng parehong uri ng halaman sa parehong lugar taon-taon. Ang crop rotation ay makakatulong sa pagwasak ng mga siklo ng buhay ng beetle.\n" +
                            "2.\tPanatilihing Malinis ang Paligid ng Taniman\n" +
                            "Linisin ang mga labi ng halaman at mga damo sa paligid ng taniman upang mabawasan ang mga tirahan ng mga leaf beetle.\n" +
                            "3.\t Gumamit ng Natural na Predators\n" +
                            "Ang mga natural na predators tulad ng ladybugs at lacewings ay nakakatulong sa pagkontrol ng populasyon ng leaf beetle. Hikayatin ang mga ito sa iyong taniman.\n" +
                            "4.\tPag-aalaga sa Malusog na Halaman\n" +
                            "Ang malulusog na halaman ay mas matatag laban sa mga peste. Siguraduhing may tamang nutrisyon at tubig ang mga halaman upang mapanatili silang malakas.\n",
                    "1.\tRegular na Pag-inspeksyon ng Taniman\n" +
                            "Regular na suriin ang mga halaman upang maagang matukoy ang infestasyon. Ang maagang pagtukoy ay makakatulong sa agarang pagkuha ng mga hakbang para sa paggamot.\n" +
                            "2.\tPanatilihing Malinis ang Paligid ng Taniman\n" +
                            "Tanggalin ang mga damo at labi ng halaman sa paligid ng taniman. Ang mga damo ay maaaring maging kanlungan ng grasshopper.\n" +
                            "3.\tPag-aalaga sa Malusog na Halaman\n" +
                            "Ang mga malulusog na halaman ay mas matatag laban sa mga peste. Siguraduhing sapat ang nutrisyon at tubig sa mga tanim.\n" +
                            "4.\tMag-plant ng Trap Crops\n" +
                            "Magtanim ng mga trap crops na mas kaakit-akit sa grasshopper upang ilihis ang kanilang atensyon mula sa mga pangunahing tanim.\n" +
                            "5.\tGumamit ng Natural na Predators\n" +
                            "Hikayatin ang mga natural na kalaban ng grasshopper tulad ng mga ibon at dragonflies sa paligid ng taniman upang makatulong sa pagkontrol ng populasyon ng grasshopper.\n",
                    "Unable to recognize the pest. No recommendation available."
            };

            String[] tipstextt2 = {
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
                            "•\tMaagang Pag-detect: Regular na inspeksyon ng palayan upang matukoy agad ang mga sintomas ng leaf smut. Agad na magsagawa ng mga hakbang kung makikita ang mga unang sintoms upang hindi ito lumala.\n",
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

            String[] tipstextt3 = {
                     "Magtanim ng iba-ibang uri ng palay upang mabawasan ang panganib ng pag-atake. Ang pagpapanatili ng natural na kaaway tulad ng mga gagamba at mga ibon ay makakatulong upang kontrolin ang populasyon ng peste.",
                "Iwasan ang sobrang paggamit ng nitrogen-based fertilizers dahil mas naaakit ang peste sa malulusog na dahon.",
                "Panatilihing malinis ang paligid ng taniman at alisin ang mga natuyong bahagi ng tanim pagkatapos ng ani.",
                "Gumamit ng organic farming methods upang hikayatin ang natural predators tulad ng wasps at lady beetles.",
                "Magtanim ng palay sa tamang panahon upang maiwasan ang peak season ng peste.",
                "Iwasan ang masyadong malapit na pagtatanim at magtanim ng iba't ibang uri ng pananim upang maiwasan ang mabilis na pagkalat ng peste.",
                "Magtanim sa mas mataas na lugar upang maiwasan ang sobrang tubig na kinahihiligan ng peste.",
                "Siguraduhing maayos ang bentilasyon ng sakahan upang maiwasan ang pagdami ng peste.",
                "Mag-apply ng tamang pestisidyo sa gabi kapag aktibo ang peste.",
                "Magtanim ng marigold sa paligid ng sakahan dahil nakakatulong ito upang itaboy ang mole cricket.",
                "Iwasan ang sobrang paggamit ng pestisidyo na pumapatay sa natural predators ng spider mites.",
                "Gumamit ng intercropping upang mabawasan ang panganib ng pag-atake.",
                "Regular na alisin ang mga damo na maaaring maging tirahan ng peste.",
                "Magtanim ng resistensiyang uri ng pananim upang mabawasan ang epekto ng peste.",
                "Maglagay ng mga bitag sa paligid ng sakahan upang mahuli ang mga adult na peste."

            };

            String classificationResult;
            String[] diagnosesArray, recommendationsArray, tipsArray;
            int selectedMaxPos;

            float minConfidenceThreshold = 0.99f;
            if (maxConfidence1 >= maxConfidence2 && maxConfidence1 >= maxConfidence3) {
                classificationResult = classes1[maxPos1];
                diagnosesArray = diagnosiss1;
                recommendationsArray = recommendations1;
                tipsArray = tipstextt1;
                selectedMaxPos = maxPos1;
                sendToDAButton.setVisibility(View.GONE);
            } else if (maxConfidence2 >= maxConfidence1 && maxConfidence2 >= maxConfidence3) {
                classificationResult = classes2[maxPos2];
                diagnosesArray = diagnosiss2;
                recommendationsArray = recommendations2;
                tipsArray = tipstextt2;
                selectedMaxPos = maxPos2;
                sendToDAButton.setVisibility(View.GONE);
            } else if (maxConfidence3 >= maxConfidence1 && maxConfidence3 >= maxConfidence2) {
                classificationResult = classes3[maxPos3];
                diagnosesArray = diagnosiss3;
                recommendationsArray = recommendations3;
                tipsArray = tipstextt3;
                selectedMaxPos = maxPos3;
                sendToDAButton.setVisibility(View.GONE);
            } else if (maxConfidence3 >= maxConfidence1 && maxConfidence3 >= maxConfidence2) {
                classificationResult = classes4[maxPos3];
                diagnosesArray = diagnosiss3;
                recommendationsArray = recommendations3;
                tipsArray = tipstextt3;
                selectedMaxPos = maxPos3;
                sendToDAButton.setVisibility(View.GONE);

            } else {
                classificationResult = "Unknown";
                diagnosis.setText("Unable to diagnose.");
                recommendation.setText("No recommendation available.");
                tipstext.setText("No tips available.");
                sendToDAButton.setVisibility(View.GONE);
                return;
            }

            result.setText(classificationResult);
            diagnosis.setText(diagnosesArray[selectedMaxPos]);
            recommendation.setText(recommendationsArray[selectedMaxPos]);
            tipstext.setText(tipsArray[selectedMaxPos]);

            saveDetectionToFirebase(
                classificationResult,
                diagnosesArray[selectedMaxPos],
                recommendationsArray[selectedMaxPos],
                tipsArray[selectedMaxPos]
            );

            result.setOnClickListener(view -> startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/search?q=" + classificationResult))));

            model1.close();
            model2.close();
            model3.close();

            if (isUnfamiliarVariant(confidenceScores1, maxConfidence1, maxPos1) ||
                isUnfamiliarVariant(confidenceScores2, maxConfidence2, maxPos2) ||
                isUnfamiliarVariant(confidenceScores3, maxConfidence3, maxPos3)) {
                sendToDAButton.setVisibility(View.VISIBLE);
                sendToDAButton.setOnClickListener(v -> saveNewVariantWithImage());
            }

        } catch (IOException e) {
            Log.e(TAG, "Error in classifyImage", e);
            e.printStackTrace();
        }
    }
    private boolean isConfidentPrediction(float[] confidenceScores, float maxConfidence, int maxPos) {
        return maxConfidence >= 0.99;
    }



    private int getMaxConfidenceIndex(float[] confidenceScores) {
        int maxPos = 0;
        float maxConfidence = 0;
        for (int i = 0; i < confidenceScores.length; i++) {
            if (confidenceScores[i] > maxConfidence) {
                maxConfidence = confidenceScores[i];
                maxPos = i;
            }
        }
        return maxPos;
    }

    private TensorBuffer preprocessAndGetInputBuffer(Bitmap image) {
        ByteBuffer byteBuffer = preprocessImage(image);
        TensorBuffer inputFeature = TensorBuffer.createFixedSize(new int[]{1, imageSize, imageSize, 3}, DataType.FLOAT32);
        inputFeature.loadBuffer(byteBuffer);
        return inputFeature;
    }

    private ByteBuffer preprocessImage(Bitmap image) {
        int imageSize = 224;
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
        return byteBuffer;
    }

    private String validateClassification(float[] confidenceScores, float maxConfidence, int maxPos) {
        if (maxConfidence >= 0.99) {
            return "CONFIDENT";
        }

        if (maxConfidence >= 0.70) {
            float secondHighestConfidence = 0;
            for (int i = 0; i < confidenceScores.length; i++) {
                if (i != maxPos && confidenceScores[i] > secondHighestConfidence) {
                    secondHighestConfidence = confidenceScores[i];
                }
            }

            if ((maxConfidence - secondHighestConfidence) > 0.30) {
                return "UNFAMILIAR_VARIANT";
            }
        }

        return "UNKNOWN";
    }

    private void saveDetectionToFirebase(String classification, String diagnosis, String recommendation, String tips) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            Log.e(TAG, "Cannot save detection: Phone number is null or empty");
            Toast.makeText(this, "Error: Cannot save detection results", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Attempting to save detection for phone number: " + phoneNumber);

        String detectionId = mDatabase.child("Farmers").child(phoneNumber).child("detections").push().getKey();

        Map<String, Object> detectionData = new HashMap<>();
        detectionData.put("classification", classification);
        detectionData.put("diagnosis", diagnosis);
        detectionData.put("recommendation", recommendation);
        detectionData.put("tips", tips);
        detectionData.put("timestamp", new Date().getTime());

        Log.d(TAG, "Detection data: " + detectionData.toString());

        if (detectionId != null) {
            mDatabase.child("Farmers")
                .child(phoneNumber)
                .child("detections")
                .child(detectionId)
                .setValue(detectionData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Detection saved successfully for user: " + phoneNumber);
                    Toast.makeText(Detection.this, "Detection saved successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to save detection for user: " + phoneNumber, e);
                    Toast.makeText(Detection.this, "Failed to save detection: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
                });
        }
    }

    private void saveNewVariantWithImage() {
        if (phoneNumber == null || phoneNumber.isEmpty() || capturedImage == null) {
            Toast.makeText(this, "Error: Cannot save new variant", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending report to DA...");
        progressDialog.show();

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "pest_variant_" + phoneNumber + "_" + timestamp + ".jpg";

        StorageReference imageRef = storageRef.child("new_variants/" + imageFileName);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        capturedImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageData = baos.toByteArray();

        imageRef.putBytes(imageData)
            .addOnSuccessListener(taskSnapshot -> {
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String variantId = mDatabase.child("NewVariant").push().getKey();

                    if (variantId != null) {
                        Map<String, Object> variantData = new HashMap<>();
                        variantData.put("phoneNumber", phoneNumber);
                        variantData.put("classification", result.getText().toString());
                        variantData.put("diagnosis", diagnosis.getText().toString());
                        variantData.put("recommendation", recommendation.getText().toString());
                        variantData.put("tips", tipstext.getText().toString());
                        variantData.put("timestamp", ServerValue.TIMESTAMP);
                        variantData.put("status", "pending");
                        variantData.put("imageUrl", uri.toString());

                        mDatabase.child("NewVariant")
                            .child(variantId)
                            .setValue(variantData)
                            .addOnSuccessListener(aVoid -> {
                                progressDialog.dismiss();
                                Toast.makeText(Detection.this,
                                    "New variant reported successfully", Toast.LENGTH_SHORT).show();
                                sendToDAButton.setVisibility(View.GONE);
                            })
                            .addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                Toast.makeText(Detection.this,
                                    "Failed to report new variant: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            });
                    }
                });
            })
            .addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(Detection.this,
                    "Failed to upload image: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            });
    }

    private boolean isUnfamiliarVariant(float[] confidenceScores, float maxConfidence, int maxPos) {
        float unfamiliarThreshold = 0.30f;
        return maxConfidence < unfamiliarThreshold;
    }
}
