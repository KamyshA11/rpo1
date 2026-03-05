package ru.iu3.myapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import java.util.Arrays;

import ru.iu3.myapp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'myapp' library on application startup.
    static {
        System.loadLibrary("myapp");
        System.loadLibrary("mbedcrypto");
    }

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        int initializationStatus = initRng();
        byte[] encryptionKey = randomBytes(16);
        byte[] originalBytes = {5, 7, 12, 21};
        byte[] encryptedBytes = encrypt(encryptionKey, originalBytes);
        byte[] decryptedBytes = decrypt(encryptionKey, encryptedBytes);;

        // Example of a call to a native method
        TextView tv = binding.sampleText;
        // tv.setText(stringFromJNI());

        // tv.setText(initializationStatus);
        tv.setText(Arrays.toString(encryptionKey));
        // tv.setText(Arrays.toString(originalBytes));
        // tv.setText(Arrays.toString(encryptedBytes));
        // tv.setText(Arrays.toString(decryptedBytes));
    }

    /**
     * A native method that is implemented by the 'myapp' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
    public static native int initRng();
    public static native byte[] randomBytes(int no);

    public static native byte[] encrypt(byte[] key, byte[] data);

    public static native byte[] decrypt(byte[] key, byte[] data);
}