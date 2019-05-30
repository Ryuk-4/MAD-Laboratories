package it.polito.mad.appcomplete;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

import static it.polito.mad.data_layer_access.FirebaseUtils.*;

public class StatisticsActivity extends AppCompatActivity {

    private static final String TAG = "StatisticsActivity";

    private LineChartView lineChartView;
    private RecyclerView mRecyclerView;

    private Map<String, Integer> popFood;
    private Map<String, FoodInfo> food;
    private List<Integer> progress;
    private List<FoodInfo> pop_food;

    private Map<String, String> times;
    private List<PointValue> yAxisValues;
    private List<AxisValue> xLabel;
    private Line line;
    private List<Line> lines;
    private LineChartData data;
    private Axis xAxisLabel, yAxisLAbel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: called");

        setContentView(R.layout.activity_statistics);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        lineChartView = findViewById(R.id.timeLineChart);
        mRecyclerView = findViewById(R.id.recyclerViewPopularFood);

        setupFirebase();

        fetchPopularFood();

        fetchTime();
    }


    private void fetchPopularFood() {
        Log.d(TAG, "fetchPopularFood: called");
        popFood = new HashMap<>();

        popularFoodBranch.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String key = "";

                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    key = dataSnapshot1.getKey();
                    break;
                }

                if (key != null) {
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.child(key).getChildren()) {
                        popFood.put(dataSnapshot1.getKey(), Integer.valueOf(dataSnapshot1.getValue().toString()));
                    }

                    fetchFood();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "onCancelled: The read failed: " + databaseError.getMessage());
            }
        });
    }

    private void fetchFood() {
        Log.d(TAG, "fetchFood: called");

        food = new HashMap<>();

        branchDailyFood.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    food.put(dataSnapshot1.getKey(), dataSnapshot1.getValue(FoodInfo.class));
                }

                displayPopularFood();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "onCancelled: The read failed: " + databaseError.getMessage());
            }
        });
    }

    private void displayPopularFood() {
        Log.d(TAG, "displayPopularFood: called");

        getListFromMap();

        
        RecyclerViewAdapterPopFood myAdapter = new RecyclerViewAdapterPopFood(StatisticsActivity.this,
                pop_food, progress);


        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(myAdapter);
    }

    private void getListFromMap() {
        progress = new ArrayList<>();
        pop_food = new ArrayList<>();

        int i = 0;

        popFood = sortByValue(popFood);

        for (String key : popFood.keySet()){
            pop_food.add(i, food.get(key));
            progress.add(i, popFood.get(key));

            i++;
        }
    }

    private static Map<String, Integer> sortByValue(Map<String, Integer> hm)
    {
        // Create a list from elements of HashMap
        List<Map.Entry<String, Integer> > list =
                new ArrayList<>(hm.entrySet());

        // Sort the list
        Collections.sort(list, new Comparator<Map.Entry<String, Integer> >() {
            public int compare(Map.Entry<String, Integer> o1,
                               Map.Entry<String, Integer> o2)
            {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        // put data from sorted list to hashmap
        Map<String, Integer> temp = new HashMap<>();
        for (Map.Entry<String, Integer> element : list) {
            temp.put(element.getKey(), element.getValue());
        }
        return temp;
    }

    private void fetchTime() {
        Log.d(TAG, "fetchTime: called");
        times = new TreeMap<>();

        timeBranch.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    times.put(data.getKey(), data.getValue().toString());
                }

                setAxisData();
                setAxisLabel();
                displayChart();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "onCancelled: The read failed: " + databaseError.getMessage());
            }
        });
    }

    private void setAxisData() {
        int i = 0;
        yAxisValues = new ArrayList<>();
        xLabel = new ArrayList<>();
        lines = new ArrayList<>();

        Log.d(TAG, "setAxisData: called");

        line = new Line(yAxisValues).setColor(Color.parseColor("#FF9800"));

        for (String key : times.keySet()) {

            xLabel.add(i, new AxisValue(i).setLabel(key));
            yAxisValues.add(new PointValue(i, Integer.valueOf(times.get(key))));

            i++;
        }

        lines.add(line);
    }

    private void setAxisLabel() {
        data = new LineChartData();
        xAxisLabel = new Axis();
        yAxisLAbel = new Axis();

        Log.d(TAG, "setAxisLabel: called");

        data.setLines(lines);

        xAxisLabel.setValues(xLabel);
        xAxisLabel.setTextSize(16);
        xAxisLabel.setTextColor(Color.parseColor("#000000"));
        data.setAxisXTop(xAxisLabel);

        yAxisLAbel.setName("#orders per time");
        yAxisLAbel.setTextSize(14);
        yAxisLAbel.setTextColor(Color.parseColor("#000000"));
        data.setAxisYLeft(yAxisLAbel);
    }

    private void displayChart() {
        Log.d(TAG, "displayChart: called");

        lineChartView.setLineChartData(data);

        Viewport viewport = new Viewport(lineChartView.getMaximumViewport());
        viewport.right = 5;
        lineChartView.setMaximumViewport(viewport);
        lineChartView.setCurrentViewport(viewport);

    }
}
