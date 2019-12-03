package com.app.binar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.app.binar.component.listener.DynamicListener;
import com.app.binar.model.Product;
import com.app.binar.transport.Body;
import com.app.binar.transport.Transporter;
import com.app.binar.transport.body.BodyBuilder;
import com.app.binar.transport.listener.TransportListener;
import com.pac.merchant.ottopay_adapter.DynamicAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements DynamicListener, TransportListener {
    List<Product> data;
    RecyclerView recyclerView;
    TextView tvEmpty;

    private final int RC_GET = 0;
    private final int RC_POST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get all product
        recyclerView = (RecyclerView) findViewById(R.id.rvProduk);
        tvEmpty = (TextView) findViewById(R.id.tvEmpty);

        recyclerView.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.VISIBLE);

        new Transporter()
                .id(RC_GET)
                .context(this)
                .url("https://monitoring-api.herokuapp.com/api/")
                .route("product/")
                .listener(this)
                .gets()
                .execute();

        Button button = (Button) findViewById(R.id.btnAdd);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddDialog();
            }
        });
    }

    private void showAddDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.add_layout, null);
        dialog.setView(dialogView);
        dialog.setCancelable(true);
        dialog.setTitle("Form Tambah Barang");

        final EditText etName = (EditText) dialogView.findViewById(R.id.etName);
        final EditText etStok = (EditText) dialogView.findViewById(R.id.etStok);
        final EditText etPemasok = (EditText) dialogView.findViewById(R.id.etPemasok);
        final EditText etTanggal = (EditText) dialogView.findViewById(R.id.etTanggal);

        dialog.setPositiveButton("Simpan", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    JSONObject object = new JSONObject();
                    object.put("name", etName.getText().toString());
                    object.put("stock", Integer.parseInt(etStok.getText().toString()));
                    object.put("pemasok", etPemasok.getText().toString());
                    object.put("date", etTanggal.getText().toString());

                    new Transporter()
                            .id(RC_POST)
                            .context(MainActivity.this)
                            .url("https://monitoring-api.herokuapp.com/api/")
                            .route("product/")
                            .listener(MainActivity.this)
                            .body(new Body().json(object))
                            .post()
                            .execute();

                } catch (Exception e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            }
        });

        dialog.setNegativeButton("Batal", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    public void onBindView(DynamicAdapter.DynamicViewHolder holder, int position, int id) {
        TextView tvName = (TextView) holder.views.get(0);
        TextView tvStock = (TextView) holder.views.get(1);
        TextView tvPemasok = (TextView) holder.views.get(2);
        TextView tvTanggal = (TextView) holder.views.get(3);

        Product product = data.get(position);
        tvName.setText(product.getName());
        tvStock.setText(String.format("Stok : %s", String.valueOf(product.getStock())));
        tvPemasok.setText(String.format("Pemasok : %s", product.getPemasok()));
        tvTanggal.setText(product.getTanggal());
    }

    @Override
    public List<View> onViewHolder(View view) {
        List<View> views = new ArrayList<>();
        TextView tvName = (TextView) view.findViewById(R.id.tvName);
        TextView tvStock = (TextView) view.findViewById(R.id.tvStock);
        TextView tvPemasok = (TextView) view.findViewById(R.id.tvPemasok);
        TextView tvTanggal = (TextView) view.findViewById(R.id.tvTanggal);

        views.add(tvName);
        views.add(tvStock);
        views.add(tvPemasok);
        views.add(tvTanggal);

        return views;
    }

    @Override
    public void onItemClick(Object... packet) {

    }

    @Override
    public void onTransportDone(Object code, Object message, Object body, int id, Object... packet) {
        try {
            data = new ArrayList<>();
            JSONObject object = new JSONObject(body.toString());

            if (id == RC_GET) {
                String resultText = object.getString("result");
                JSONArray result = new JSONArray(resultText);

                for (int i = 0; i < result.length(); i++) {
                    JSONObject o = result.getJSONObject(i);
                    Product product = new Product();
                    product.setName(o.getString("name"));
                    product.setStock(o.getInt("stock"));
                    product.setPemasok(o.getString("pemasok"));
                    product.setTanggal(o.getString("date"));

                    data.add(product);
                }

                if (data.size() > 0) {
                    recyclerView.setVisibility(View.VISIBLE);
                    tvEmpty.setVisibility(View.GONE);
                }

                DynamicAdapter adapter = new DynamicAdapter(MainActivity.this, data.size(), this, R.layout.row_product, 0);
                recyclerView.setLayoutManager(new LinearLayoutManager(this));
                recyclerView.setAdapter(adapter);
            } else {
                Toast.makeText(this, object.getString("result"), Toast.LENGTH_LONG).show();
                new Transporter()
                        .id(RC_GET)
                        .context(this)
                        .url("https://monitoring-api.herokuapp.com/api/")
                        .route("product/")
                        .listener(this)
                        .gets()
                        .execute();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTransportFail(Object code, Object message, Object body, int id, Object... packet) {
        Toast.makeText(this, code + "/" + message, Toast.LENGTH_LONG).show();
    }
}
