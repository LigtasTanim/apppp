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
import com.example.ligtastanim.ml.PestDetection;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.annotation.Nullable;

public class Detection3 extends AppCompatActivity {

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
            PestDetection model = PestDetection.newInstance(getApplicationContext());

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

            PestDetection.Outputs outputs = model.process(inputFeature0);
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
            String[] classes = {"", "", "", "", "",
                    "", "", "", "", "",
                    "", "", ""};

            String[] diagnosiss = {
                    "",

            };

            String[] recommendations = {
                    "",

            };

            String[] tipstextt = {
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
