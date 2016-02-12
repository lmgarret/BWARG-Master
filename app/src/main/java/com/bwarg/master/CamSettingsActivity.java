package com.bwarg.master;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView.BufferType;

public class CamSettingsActivity extends ActionBarActivity {

    Button settings_done;

    Spinner resolution_spinner;
    EditText width_input;
    EditText height_input;

    EditText address1_input;
    EditText address2_input;
    EditText address3_input;
    EditText address4_input;
    EditText port_input;

    Button address1_increment;
    Button address2_increment;
    Button address3_increment;
    Button address4_increment;

    Button address1_decrement;
    Button address2_decrement;
    Button address3_decrement;
    Button address4_decrement;

    RadioGroup port_group;

    int width = 640;
    int height = 480;

    int ip_ad1 = 192;
    int ip_ad2 = 168;
    int ip_ad3 = 2;
    int ip_ad4 = 1;
    int ip_port = 8080;
    int cam_number = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getResources().getString(R.string.title_settings_general));
        setContentView(R.layout.cam_settings);

        Bundle extras = getIntent().getExtras();

        ArrayAdapter<CharSequence> adapter =
                ArrayAdapter.createFromResource(this, R.array.resolution_array,
                        android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        resolution_spinner = (Spinner) findViewById(R.id.resolution_spinner);
        resolution_spinner.setAdapter(adapter);

        width_input = (EditText) findViewById(R.id.width_input);
        height_input = (EditText) findViewById(R.id.height_input);

        address1_input = (EditText) findViewById(R.id.address1_input);
        address2_input = (EditText) findViewById(R.id.address2_input);
        address3_input = (EditText) findViewById(R.id.address3_input);
        address4_input = (EditText) findViewById(R.id.address4_input);
        port_input = (EditText) findViewById(R.id.port_input);

        port_group = (RadioGroup) findViewById(R.id.port_radiogroup);

        if (extras != null) {
            width = extras.getInt("width", width);
            height = extras.getInt("height", height);

            ip_ad1 = extras.getInt("ip_ad1", ip_ad1);
            ip_ad2 = extras.getInt("ip_ad2", ip_ad2);
            ip_ad3 = extras.getInt("ip_ad3", ip_ad3);
            ip_ad4 = extras.getInt("ip_ad4", ip_ad4);
            ip_port = extras.getInt("ip_port", ip_port);

            Log.d("MJPEG_Cam"+cam_number, " received URL "+getURL()+ " in CamSettingsActivity.");
            cam_number = extras.getInt("cam_number", cam_number);

            width_input.setText(String.valueOf(width));
            height_input.setText(String.valueOf(height));
            resolution_spinner.setSelection(adapter.getCount() - 1);

            address1_input.setText(String.valueOf(ip_ad1));
            address2_input.setText(String.valueOf(ip_ad2));
            address3_input.setText(String.valueOf(ip_ad3));
            address4_input.setText(String.valueOf(ip_ad4));
            port_input.setText(String.valueOf(ip_port));

            if(cam_number == 1){
                //title.setText(getResources().getString(R.string.title_settings_left));
                setTitle(getResources().getString(R.string.title_settings_left));
            }else if (cam_number == 2){
                //title.setText(getResources().getString(R.string.title_settings_right));
                setTitle(getResources().getString(R.string.title_settings_right));
            }
        }else{
            Log.d("MJPEG_Cam"+cam_number, " null args received");
        }

        resolution_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View viw, int arg2, long arg3) {
                Spinner spinner = (Spinner) parent;
                String item = (String) spinner.getSelectedItem();
                if (item.equals("640x480")) {
                    width = 640;
                    height = 480;
                } else if (item.equals("480x640")) {
                    width = 480;
                    height = 640;
                } else if (item.equals("320x240")) {
                    width = 320;
                    height = 240;
                } else if (item.equals("240x320")) {
                    width = 240;
                    height = 320;
                } else if (item.equals("176x144")) {
                    width = 176;
                    height = 144;
                } else if (item.equals("144x176")) {
                    width = 144;
                    height = 176;
                }
                width_input.setText(String.valueOf(width));
                height_input.setText(String.valueOf(height));
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        address1_increment = (Button) findViewById(R.id.address1_increment);
        address1_increment.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        String s = address1_input.getText().toString();
                        int val = ip_ad1;
                        if (!"".equals(s)) {
                            val = Integer.parseInt(s);
                        }
                        if (val >= 0 && val < 255) {
                            val += 1;
                        } else if (val < 0) {
                            val = 0;
                        } else if (val >= 255) {
                            val = 255;
                        }

                        ip_ad1 = val;
                        address1_input.setText(String.valueOf(ip_ad1), BufferType.NORMAL);

                    }
                }
        );
        address2_increment = (Button) findViewById(R.id.address2_increment);
        address2_increment.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        String s = address2_input.getText().toString();
                        int val = ip_ad2;
                        if (!"".equals(s)) {
                            val = Integer.parseInt(s);
                        }
                        if (val >= 0 && val < 255) {
                            val += 1;
                        } else if (val < 0) {
                            val = 0;
                        } else if (val >= 255) {
                            val = 255;
                        }

                        ip_ad2 = val;
                        address2_input.setText(String.valueOf(ip_ad2), BufferType.NORMAL);

                    }
                }
        );
        address3_increment = (Button) findViewById(R.id.address3_increment);
        address3_increment.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        String s = address3_input.getText().toString();
                        int val = ip_ad3;
                        if (!"".equals(s)) {
                            val = Integer.parseInt(s);
                        }
                        if (val >= 0 && val < 255) {
                            val += 1;
                        } else if (val < 0) {
                            val = 0;
                        } else if (val >= 255) {
                            val = 255;
                        }

                        ip_ad3 = val;
                        address3_input.setText(String.valueOf(ip_ad3), BufferType.NORMAL);

                    }
                }
        );
        address4_increment = (Button) findViewById(R.id.address4_increment);
        address4_increment.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        String s = address4_input.getText().toString();
                        int val = ip_ad4;
                        if (!"".equals(s)) {
                            val = Integer.parseInt(s);
                        }
                        if (val >= 0 && val < 255) {
                            val += 1;
                        } else if (val < 0) {
                            val = 0;
                        } else if (val >= 255) {
                            val = 255;
                        }

                        ip_ad4 = val;
                        address4_input.setText(String.valueOf(ip_ad4), BufferType.NORMAL);

                    }
                }
        );

        address1_decrement = (Button) findViewById(R.id.address1_decrement);
        address1_decrement.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        String s = address1_input.getText().toString();
                        int val = ip_ad1;
                        if (!"".equals(s)) {
                            val = Integer.parseInt(s);
                        }
                        if (val > 0 && val <= 255) {
                            val -= 1;
                        } else if (val <= 0) {
                            val = 0;
                        } else if (val > 255) {
                            val = 255;
                        }

                        ip_ad1 = val;
                        address1_input.setText(String.valueOf(ip_ad1), BufferType.NORMAL);

                    }
                }
        );

        address2_decrement = (Button) findViewById(R.id.address2_decrement);
        address2_decrement.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        String s = address2_input.getText().toString();
                        int val = ip_ad2;
                        if (!"".equals(s)) {
                            val = Integer.parseInt(s);
                        }
                        if (val > 0 && val <= 255) {
                            val -= 1;
                        } else if (val <= 0) {
                            val = 0;
                        } else if (val > 255) {
                            val = 255;
                        }

                        ip_ad2 = val;
                        address2_input.setText(String.valueOf(ip_ad2), BufferType.NORMAL);

                    }
                }
        );
        address3_decrement = (Button) findViewById(R.id.address3_decrement);
        address3_decrement.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        String s = address3_input.getText().toString();
                        int val = ip_ad3;
                        if (!"".equals(s)) {
                            val = Integer.parseInt(s);
                        }
                        if (val > 0 && val <= 255) {
                            val -= 1;
                        } else if (val <= 0) {
                            val = 0;
                        } else if (val > 255) {
                            val = 255;
                        }

                        ip_ad3 = val;
                        address3_input.setText(String.valueOf(ip_ad3), BufferType.NORMAL);

                    }
                }
        );
        address4_decrement = (Button) findViewById(R.id.address4_decrement);
        address4_decrement.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        String s = address4_input.getText().toString();
                        int val = ip_ad4;
                        if (!"".equals(s)) {
                            val = Integer.parseInt(s);
                        }
                        if (val > 0 && val <= 255) {
                            val -= 1;
                        } else if (val <= 0) {
                            val = 0;
                        } else if (val > 255) {
                            val = 255;
                        }

                        ip_ad4 = val;
                        address4_input.setText(String.valueOf(ip_ad4), BufferType.NORMAL);

                    }
                }
        );

        port_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.port_8080) {
                    port_input.setText(getString(R.string.port_8080));
                }
            }
        });

        settings_done = (Button) findViewById(R.id.settings_done);
        settings_done.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {

                        String s;

                        s = width_input.getText().toString();
                        if (!"".equals(s)) {
                            width = Integer.parseInt(s);
                        }
                        s = height_input.getText().toString();
                        if (!"".equals(s)) {
                            height = Integer.parseInt(s);
                        }
                        s = address1_input.getText().toString();
                        if (!"".equals(s)) {
                            ip_ad1 = Integer.parseInt(s);
                        }
                        s = address2_input.getText().toString();
                        if (!"".equals(s)) {
                            ip_ad2 = Integer.parseInt(s);
                        }
                        s = address3_input.getText().toString();
                        if (!"".equals(s)) {
                            ip_ad3 = Integer.parseInt(s);
                        }
                        s = address4_input.getText().toString();
                        if (!"".equals(s)) {
                            ip_ad4 = Integer.parseInt(s);
                        }

                        s = port_input.getText().toString();
                        if (!"".equals(s)) {
                            ip_port = Integer.parseInt(s);
                        }


                        Intent intent = new Intent();
                        intent.putExtra("width", width);
                        intent.putExtra("height", height);
                        intent.putExtra("ip_ad1", ip_ad1);
                        intent.putExtra("ip_ad2", ip_ad2);
                        intent.putExtra("ip_ad3", ip_ad3);
                        intent.putExtra("ip_ad4", ip_ad4);
                        intent.putExtra("ip_port", ip_port);
                        intent.putExtra("cam_number", cam_number);

                        Log.d("MJPEG_CAM"+cam_number, "Sending URL "+getURL()+" back to MjpegActivty");
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }
        );
    }
    private String getURL(){
        StringBuilder sb = new StringBuilder();
        String s_http = "http://";
        String s_dot = ".";
        String s_colon = ":";
        String s_slash = "/";
        sb.append(s_http);
        sb.append(ip_ad1);
        sb.append(s_dot);
        sb.append(ip_ad2);
        sb.append(s_dot);
        sb.append(ip_ad3);
        sb.append(s_dot);
        sb.append(ip_ad4);
        sb.append(s_colon);
        sb.append(ip_port);
        sb.append(s_slash);
        return new String(sb);
    }
}
