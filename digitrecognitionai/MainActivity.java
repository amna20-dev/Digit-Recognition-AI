package com.example.digitrecognitionai;

import android.os.Bundle;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    DrawingView drawingView;
    Button btnClear, btnPredict;
    TextView txtResult, txtConfidence;

    private DigitClassifier digitClassifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Connect XML views
        drawingView = findViewById(R.id.drawingView);
        btnClear = findViewById(R.id.btnClear);
        btnPredict = findViewById(R.id.btnPredict);
        txtResult = findViewById(R.id.txtResult);
        txtConfidence = findViewById(R.id.txtConfidence);

        // Load AI Model
        try {
            digitClassifier = new DigitClassifier(this);

            Toast.makeText(
                    this,
                    "AI Model Loaded Successfully!",
                    Toast.LENGTH_SHORT
            ).show();

        } catch (IOException e) {
            Toast.makeText(
                    this,
                    "AI Model Loading Failed!",
                    Toast.LENGTH_LONG
            ).show();
        }

        // Clear Drawing Button
        btnClear.setOnClickListener(v -> {

            drawingView.clearDrawing();

            txtResult.setText("Prediction: -");
            txtConfidence.setText("Confidence: -");

        });

        // Predict Number Button
        btnPredict.setOnClickListener(v -> {

            if (digitClassifier == null) {
                Toast.makeText(
                        this,
                        "AI Model is not loaded!",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            Bitmap bitmap = drawingView.getBitmap();

            // Check empty drawing
            if (isDrawingEmpty(bitmap)) {
                Toast.makeText(
                        this,
                        "Please draw a number first!",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            try {

                // AI Prediction
                float[] results = digitClassifier.predict(bitmap);

                int predictedDigit = 0;
                float highestConfidence = results[0];

                for (int i = 1; i < results.length; i++) {

                    if (results[i] > highestConfidence) {
                        highestConfidence = results[i];
                        predictedDigit = i;
                    }
                }

                // Display Result
                txtResult.setText(
                        "Prediction: " + predictedDigit
                );

                txtConfidence.setText(
                        "Confidence: " +
                                String.format("%.2f%%",
                                        highestConfidence * 100)
                );

            } catch (Exception e) {

                Toast.makeText(
                        this,
                        "Prediction Failed: " + e.getMessage(),
                        Toast.LENGTH_LONG
                ).show();

            }
        });
    }

    // Check whether drawing area is empty
    private boolean isDrawingEmpty(Bitmap bitmap) {

        for (int y = 0; y < bitmap.getHeight(); y += 5) {

            for (int x = 0; x < bitmap.getWidth(); x += 5) {

                if (Color.red(bitmap.getPixel(x, y)) < 240) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    protected void onDestroy() {

        if (digitClassifier != null) {
            digitClassifier.close();
        }

        super.onDestroy();
    }
}
