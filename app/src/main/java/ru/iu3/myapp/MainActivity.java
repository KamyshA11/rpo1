package ru.iu3.myapp;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.util.Arrays;

import ru.iu3.myapp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements TransactionEvents {

    private ActivityMainBinding binding;
    ActivityResultLauncher activityResultLauncher;
    private String pin;


    // Used to load the 'myapp' library on application startup.
    static {
        System.loadLibrary("myapp");
        System.loadLibrary("mbedcrypto");
    }

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


        log("Enc Key" + Arrays.toString(encryptionKey));
        log("Orig Bytes" + Arrays.toString(originalBytes));
        log("Enc Bytes" + Arrays.toString(encryptedBytes));
        log("Dec Bytes" + Arrays.toString(decryptedBytes));
        // Example of a call to a native method
        TextView tv = binding.sampleText;
        // tv.setText(stringFromJNI());
        // Toast.makeText(this, "Hello", Toast.LENGTH_SHORT).show();

        activityResultLauncher  = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback() {
                @Override
                public void onActivityResult(Object o) {
                    ActivityResult result = (ActivityResult) o;
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        // обработка результата

                        // String pin = data.getStringExtra("pin");
                        // Toast.makeText(MainActivity.this, pin, Toast.LENGTH_SHORT).show();

                        pin = data.getStringExtra("pin");
                        synchronized (MainActivity.this) {
                            MainActivity.this.notifyAll();
                        }
                    }
                }
            });
    }

    public static byte[] stringToHex(String s)
    {
        byte[] hex;
        try
        {
            hex = Hex.decodeHex(s.toCharArray());
        }
        catch (DecoderException ex)
        {
            hex = null;
        }
        return hex;
    }

    // public void onButtonClick(View v)
    // {
    //     byte[] key = stringToHex("0123456789ABCDEF0123456789ABCDE0");
    //     byte[] enc = encrypt(key, stringToHex("000000000000000102"));
    //     byte[] dec = decrypt(key, enc);
    //     String s = new String(Hex.encodeHex(dec)).toUpperCase();
    //     Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    // }

    @Override
    public String enterPin(int ptc, String amount) {
        pin = new String();
        log("enterPin работает");
        Intent it = new Intent(MainActivity.this, PinpadActivity.class);
        it.putExtra("ptc", ptc);
        it.putExtra("amount", amount);
        synchronized (MainActivity.this) {
            activityResultLauncher.launch(it);
            try {
                MainActivity.this.wait();
            } catch (Exception ex) {
                log(ex.toString());
            }
        }
        return pin;
    }

    @Override
    public void transactionResult(boolean result) {
        runOnUiThread(()-> {
            Toast.makeText(MainActivity.this,
                    result ? "ok" : "failed", Toast.LENGTH_SHORT).show();
        });
    }

    public void onButtonClick(View v)
    {
        // Intent it = new Intent(this, PinpadActivity.class);
        // startActivity(it);
        // activityResultLauncher.launch(it);

        byte[] trd = stringToHex("9F0206000000000100");
        boolean ok = transaction(trd);
    }

    /**
     * A native method that is implemented by the 'myapp' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    public static native void log(String str);
    public static native int initRng();
    public static native byte[] randomBytes(int no);
    public static native byte[] encrypt(byte[] key, byte[] data);
    public static native byte[] decrypt(byte[] key, byte[] data);

    public native boolean transaction(byte[] trd);
}