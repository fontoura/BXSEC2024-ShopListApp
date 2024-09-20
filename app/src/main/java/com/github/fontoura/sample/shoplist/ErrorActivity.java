package com.github.fontoura.sample.shoplist;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.fontoura.sample.shoplist.databinding.ActivityErrorBinding;

public class ErrorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityErrorBinding binding = ActivityErrorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}
