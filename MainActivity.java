package com.example.house_indiv_mini_project;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity{
    Bitmap bitmap;
    Uri selectedImage;
    ImageView imageView;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button gallery = findViewById(R.id.gallery);
        gallery.setOnClickListener(new View.OnClickListener() {


            public void onClick(View view){
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                //Intent.setType("image/*");
                startActivityForResult(intent, 3);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {

            ImageView imageView = findViewById(R.id.imageView);
            try {
                //receive the data from selected image
                InputStream inputStream = getContentResolver().openInputStream(data.getData());
                //create a bitmap of that image
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                //set the imageView bitmap to the selected image bitmap
                imageView.setImageBitmap(bitmap);
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public static Bitmap invertImage(Bitmap original){

        Bitmap finalimage = Bitmap.createBitmap(original.getWidth(), original.getHeight(), original.getConfig());

        int A;
        int R;
        int G;
        int B;
        int pixelColor;
        int height = original.getHeight();
        int width = original.getWidth();

        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                pixelColor = original.getPixel(x, y);
                A = Color.alpha(pixelColor);
                R = 255 - Color.red(pixelColor);
                G = 255 - Color.green(pixelColor);
                B = 255 - Color.blue(pixelColor);
                finalimage.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }
        return finalimage;
    }

    public void classify(View view) throws IOException {
        // Gets Bitmap of loaded image
        imageView = findViewById(R.id.imageView);
        bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), selectedImage);

        Module module = Module.load("vgg16_model_scripted.py");
        Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(bitmap,
                TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB);
        Tensor outputTensor = module.forward(IValue.from(inputTensor)).toTensor();
        float[] scores = outputTensor.getDataAsFloatArray();

        Bitmap newImage = invertImage(bitmap);
        imageView.setImageBitmap(newImage);
    }
}