package it.gilvegliach.autocrop;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity {
  AutoCropImageView iv;
  TextView tv;
  ToggleButton toggle;
  Bitmap[] bms;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    iv = (AutoCropImageView) findViewById(R.id.iv);
    tv = (TextView) findViewById(R.id.tv);
    toggle = (ToggleButton) findViewById(R.id.toggle);
    new AsyncTask<Void, Void, Void>() {
      @Override
      protected Void doInBackground(Void[] params) {
        decodeBitmaps();
        return null;
      }

      @Override
      protected void onPostExecute(Void o) {
        crop(0);
      }
    }.execute();
  }

  private void decodeBitmaps() {
    bms = new Bitmap[5];
    bms[0] = BitmapFactory.decodeResource(getResources(), R.drawable.image1);
    bms[1] = BitmapFactory.decodeResource(getResources(), R.drawable.image2);
    bms[2] = BitmapFactory.decodeResource(getResources(), R.drawable.image3);
    bms[3] = BitmapFactory.decodeResource(getResources(), R.drawable.image4);
    bms[4] = BitmapFactory.decodeResource(getResources(), R.drawable.image5);
  }

  public void onRadioButtonClicked(View v) {
    switch (v.getId()) {
      case R.id.btn1:
        crop(0);
        break;
      case R.id.btn2:
        crop(1);
        break;
      case R.id.btn3:
        crop(2);
        break;
      case R.id.btn4:
        crop(3);
        break;
      default:
        crop(4);
        break;
    }
  }

  private void crop(int index) {
    long millis = toggle.isChecked()
        ? iv.setImageBitmapWithAutocropPar(bms[index])
        : iv.setImageBitmapWithAutocrop(bms[index]);
    tv.setText(String.valueOf(millis));
  }
}