package com.example.digitrecognitionai;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class DigitClassifier {

    private Interpreter interpreter;

    public DigitClassifier(Context context) throws IOException {

        MappedByteBuffer model = loadModel(context);

        interpreter = new Interpreter(model);
    }

    private MappedByteBuffer loadModel(Context context)
            throws IOException {

        android.content.res.AssetFileDescriptor fileDescriptor =
                context.getAssets().openFd("digit_model.tflite");

        FileInputStream inputStream =
                new FileInputStream(fileDescriptor.getFileDescriptor());

        FileChannel fileChannel = inputStream.getChannel();

        return fileChannel.map(
                FileChannel.MapMode.READ_ONLY,
                fileDescriptor.getStartOffset(),
                fileDescriptor.getDeclaredLength()
        );
    }



    public float[] predict(Bitmap bitmap) {

        Bitmap resized = Bitmap.createScaledBitmap(
                bitmap, 28, 28, true
        );

        // Correct input shape: [1][28][28]
        float[][][] input = new float[1][28][28];

        for (int y = 0; y < 28; y++) {

            for (int x = 0; x < 28; x++) {

                int pixel = resized.getPixel(x, y);

                int gray = (
                        Color.red(pixel) +
                                Color.green(pixel) +
                                Color.blue(pixel)
                ) / 3;

                input[0][y][x] = (255 - gray) / 255.0f;
            }
        }

        // Output shape: [1][10]
        float[][] output = new float[1][10];

        interpreter.run(input, output);

        return output[0];
    }


    public void close() {

        if (interpreter != null) {
            interpreter.close();
        }
    }
}

