package com.example.set3;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private TextView display;

    private ArrayList<String> historyList = new ArrayList<>();
    private String currentInput = "";
    private Double previousResult = null;
    private String currentOperator = null;

    private boolean justPressedEquals = false;
    private boolean isError = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        display = findViewById(R.id.display);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupButtons();

    }

    private void setupButtons() {

        int[] digitButtons = {
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3,
                R.id.btn4, R.id.btn5, R.id.btn6,
                R.id.btn7, R.id.btn8, R.id.btn9
        };

        for (int id : digitButtons) {
            Button btn = findViewById(id);
            btn.setOnClickListener(v -> onDigitClicked(((Button) v).getText().toString()));
        }

        findViewById(R.id.btnPlus).setOnClickListener(v -> onOperatorClicked("+"));
        findViewById(R.id.btnMinus).setOnClickListener(v -> onOperatorClicked("-"));
        findViewById(R.id.btnMultiply).setOnClickListener(v -> onOperatorClicked("*"));
        findViewById(R.id.btnDivide).setOnClickListener(v -> onOperatorClicked("/"));
        findViewById(R.id.btnPower).setOnClickListener(v -> onOperatorClicked("^"));

        findViewById(R.id.btnEquals).setOnClickListener(v -> onEqualsClicked());
        findViewById(R.id.btnClear).setOnClickListener(v -> resetCalculator());

        findViewById(R.id.btnBin).setOnClickListener(v -> convertBase(2));
        findViewById(R.id.btnOct).setOnClickListener(v -> convertBase(8));
        findViewById(R.id.btnHex).setOnClickListener(v -> convertBase(16));

        findViewById(R.id.btnHistory).setOnClickListener(v -> {

            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);

            intent.putStringArrayListExtra("history", historyList);

            startActivity(intent);
        });
    }

    // =============================
    // DIGITS
    // =============================
    private void onDigitClicked(String digit) {
        if (isError) return;

        if (justPressedEquals) {
            resetCalculator();
        }

        currentInput += digit;
        display.setText(currentInput);
    }

    // =============================
    // OPERATORS
    // =============================
    private void onOperatorClicked(String op) {
        if (isError) return;

        // Continue after "="
        if (justPressedEquals) {
            currentOperator = op;
            justPressedEquals = false;
            return;
        }

        if (currentInput.isEmpty() && previousResult == null) return;

        Double inputValue = null;
        if (!currentInput.isEmpty()) {
            inputValue = Double.parseDouble(currentInput);
        }

        if (previousResult == null) {
            previousResult = inputValue;
        } else if (inputValue != null && currentOperator != null) {
            previousResult = calculate(previousResult, inputValue, currentOperator);
            display.setText(format(previousResult));
        }

        currentOperator = op;
        currentInput = "";
    }

    // =============================
    // EQUALS
    // =============================
    private void onEqualsClicked() {
        if (isError) return;

        if (previousResult == null || currentOperator == null || currentInput.isEmpty()) return;

        double inputValue = Double.parseDouble(currentInput);
        double result = calculate(previousResult, inputValue, currentOperator);

        if (isError) {
            display.setText(getString(R.string.error_text));
            return;
        }

        //Saving text to history
        String historyEntry =
                format(previousResult) + " " +
                        currentOperator + " " +
                        currentInput + " = " +
                        format(result);

        historyList.add(historyEntry);

        display.setText(format(result));

        previousResult = result;
        currentInput = "";
        currentOperator = null;
        justPressedEquals = true;
    }

    // =============================
    // CALCULATE
    // =============================
    private double calculate(double a, double b, String op) {
        switch (op) {
            case "+":
                return a + b;
            case "-":
                return a - b;
            case "*":
                return a * b;
            case "/":
                if (b == 0) {
                    isError = true;
                    return 0;
                }
                return a / b;
            case "^":
                return Math.pow(a, b);
            default:
                return b;
        }
    }

    // =============================
    // RESET
    // =============================
    private void resetCalculator() {
        currentInput = "";
        previousResult = null;
        currentOperator = null;
        justPressedEquals = false;
        isError = false;
        display.setText(getString(R.string.display_default));
    }

    // =============================
    // FORMAT
    // =============================
    private String format(double value) {
        if (value == (int) value) {
            return String.valueOf((int) value);
        } else {
            return String.valueOf(value);
        }
    }

    // =============================
    // CONVERT BASE
    // =============================

    private void convertBase(int base) {

        String text = display.getText().toString();

        try {

            double value = Double.parseDouble(text);

            // Only allow integers
            if (value % 1 != 0) {
                display.setText(getString(R.string.error_text));
                isError = true;
                return;
            }

            int intValue = (int) value;

            String result;

            switch (base) {

                case 2:
                    result = Integer.toBinaryString(intValue);
                    break;

                case 8:
                    result = Integer.toOctalString(intValue);
                    break;

                case 16:
                    result = Integer.toHexString(intValue).toUpperCase();
                    break;

                default:
                    result = String.valueOf(intValue);
            }

            display.setText(result);

        } catch (Exception e) {
            display.setText(getString(R.string.error_text));
            isError = true;
        }
    }

}

