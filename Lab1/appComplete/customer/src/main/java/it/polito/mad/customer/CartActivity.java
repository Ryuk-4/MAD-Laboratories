package it.polito.mad.customer;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import java.util.List;

public class CartActivity extends AppCompatActivity{

    private LinearLayout cart;
    private TextView totalAmount;
    private String restId;
    private Button buttonSend, buttonDiscard;
    private List<OrderRecap> list;
    private Spinner spinnerTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        Toolbar toolbar = findViewById(R.id.toolbar_cart);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        cart = findViewById(R.id.customer_cart);
        totalAmount = findViewById(R.id.total_amount);
        buttonSend = findViewById(R.id.button_send);
        spinnerTime = findViewById(R.id.spinner_time);
        buttonDiscard = findViewById(R.id.button_discard);

        Bundle bundle = getIntent().getExtras();
        list = bundle.getParcelableArrayList("data");
        restId = bundle.getString("restId");
        int partial = 0;

        for (OrderRecap orderRecap : list)
        {
            LinearLayout linearLayout = new LinearLayout(this);
            linearLayout.setOrientation(LinearLayout.VERTICAL);

            String name = orderRecap.getName();
            String quantity = orderRecap.getQuantity();
            String price = orderRecap.getPrice();

            partial += Integer.parseInt(price) * Integer.parseInt(quantity);

            TextView tvName = new TextView(this);
            tvName.setText(name);
            tvName.setPadding(10, 0, 0, 0);
            tvName.setTextSize(18);
            TextView tvQuantity = new TextView(this);
            tvQuantity.setText(quantity+"pcs");
            tvQuantity.setTextSize(18);
            tvQuantity.setPadding(10, 0, 0, 0);
            TextView tvPrice = new TextView(this);
            tvPrice.setText(price+"€");
            tvPrice.setTextSize(18);
            tvPrice.setGravity(Gravity.RIGHT);
            tvPrice.setPadding(0, 0, 10, 0);

            linearLayout.addView(tvName);
            linearLayout.addView(tvQuantity);
            linearLayout.addView(tvPrice);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            layoutParams.setMargins(0, 10, 0, 10);
            linearLayout.setBackground(this.getDrawable(R.drawable.rounded_corner_white));

            cart.addView(linearLayout, layoutParams);
        }

        totalAmount.setText("Total amount: "+Integer.toString(partial)+"€");


        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("restaurants_tmp").child(restId).child("orders").child("incoming").push();
                StringBuffer totalOrder = new StringBuffer("");
                for (OrderRecap o : list)
                {
                    for (int i = 0 ; i < Integer.parseInt(o.getQuantity()) ; i++)
                    {
                        totalOrder.append(o.getName()).append(", ");
                    }
                }

                databaseReference.child("customer").setValue(FirebaseAuth.getInstance().getUid());
                databaseReference.child("order").setValue(totalOrder.toString());
                databaseReference.child("time").setValue(spinnerTime.getSelectedItem().toString());

                DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference("customers").child(FirebaseAuth.getInstance().getUid());
                databaseReference1.child("previous_order").push().setValue(restId);
                setResult(RESULT_OK);
                finish();
            }
        });

        buttonDiscard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences("orders_info", Context.MODE_PRIVATE);

                sharedPreferences.edit().clear().commit();
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
        super.onBackPressed();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        setResult(RESULT_CANCELED);
        finish();
        return true;
    }
}
