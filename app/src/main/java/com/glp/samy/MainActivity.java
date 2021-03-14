package com.glp.samy;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {



    TextView tvtDia, tvtHora, tvtHumedad, tvtTemperatura, tvtTierra, tvtEncendido, tvtApagado, tvtMinutos, tvtMinimo, tvtMaximo;
    ImageButton btnDesconectar, guardarDatos,
            subirEncendido, bajarEncendido, subirApagado, bajarApagado,
            subirMinutos, bajarMinutos,
            subirMinimo, bajarMinimo, subirMaximo, bajarMaximo;


    //-------------------------------------------
    Handler bluetoothIn;
    final int RECIEVE_MESSAGE = 1;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder sb = new StringBuilder();
    private ConnectedThread MyConexionBT;
    // Identificador unico de servicio - SPP UUID
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // String para la direccion MAC
    private static String address = null;
    //-------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case RECIEVE_MESSAGE:                            // if receive massage
                        byte[] readBuf = (byte[]) msg.obj;
                        String strIncom = new String(readBuf, 0, msg.arg1); // create string from bytes array
                        sb.append(strIncom);                                      // append string
                        int endOfLineIndex = sb.indexOf("\r\n");                  // determine the end-of-line
                        if (endOfLineIndex > 0) {                                  // if end-of-line,
                            String sbprint = sb.substring(0, endOfLineIndex);     // extract string
                            sb.delete(0, sb.length());                           // and clear
                            if (sbprint.contains("Di")) {
                                String[] splitted = sbprint.split("Di");
                                for (int i=0; i<splitted.length; i++){
                                    tvtDia.setText(splitted[i]);  // update TextView
                                }
                            }
                            if (sbprint.contains("Hr")){
                                String[] splitted = sbprint.split("Hr");
                                for (int i=0; i<splitted.length; i++){
                                    tvtHora.setText(splitted[i]);  // update TextView
                                }
                            }
                            if (sbprint.contains("Tp")){
                                String[] splitted = sbprint.split("Tp");
                                for (int i=0; i<splitted.length; i++){
                                    tvtTemperatura.setText(splitted[i]);  // update TextView
                                }
                            }
                            if (sbprint.contains("Ha")){
                                String[] splitted = sbprint.split("Ha");
                                for (int i=0; i<splitted.length; i++){
                                    tvtHumedad.setText(splitted[i]);  // update TextView
                                }
                            }
                            if (sbprint.contains("Tr")){
                                String[] splitted = sbprint.split("Tr");
                                for (int i=0; i<splitted.length; i++){
                                    tvtTierra.setText(splitted[i]);  // update TextView
                                }
                            }
                            if (sbprint.contains("En")){
                                String[] splitted = sbprint.split("En");
                                for (int i=0; i<splitted.length; i++){
                                    tvtEncendido.setText(splitted[i]);  // update TextView
                                }
                            }
                            if (sbprint.contains("Ap")){
                                String[] splitted = sbprint.split("Ap");
                                for (int i=0; i<splitted.length; i++){
                                    tvtApagado.setText(splitted[i]);  // update TextView
                                }
                            }
                            if (sbprint.contains("Mn")){
                                String[] splitted = sbprint.split("Mn");
                                for (int i=0; i<splitted.length; i++){
                                    tvtMinutos.setText(splitted[i]);  // update TextView
                                }
                            }
                            if (sbprint.contains("Mi")){
                                String[] splitted = sbprint.split("Mi");
                                for (int i=0; i<splitted.length; i++){
                                    tvtMinimo.setText(splitted[i]);  // update TextView
                                }
                            }
                            if (sbprint.contains("Ma")){
                                String[] splitted = sbprint.split("Ma");
                                for (int i=0; i<splitted.length; i++){
                                    tvtMaximo.setText(splitted[i]);  // update TextView
                                }
                            }
                        }
                        break;
                }
            }
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter(); // get Bluetooth adapter
        VerificarEstadoBT();

        tvtDia         = findViewById(R.id.tvtDia);
        tvtHora        = findViewById(R.id.tvtHora);
        tvtTemperatura = findViewById(R.id.tvtTemperatura);
        tvtHumedad     = findViewById(R.id.tvtHumedad);
        tvtTierra      = findViewById(R.id.tvtTierra);
        tvtEncendido      = findViewById(R.id.tvtEncendido);
        tvtApagado     = findViewById(R.id.tvtApagado);
        tvtMinutos      = findViewById(R.id.tvtMinutos);
        tvtMinimo     = findViewById(R.id.tvtMinimo);
        tvtMaximo      = findViewById(R.id.tvtMaximo);
        btnDesconectar = findViewById(R.id.btnDesconectar);
        guardarDatos = findViewById(R.id.guardarDatos);
        subirEncendido = findViewById(R.id.subirEncendido);
        bajarEncendido = findViewById(R.id.bajarEncendido);
        subirApagado = findViewById(R.id.subirApagado);
        bajarApagado = findViewById(R.id.bajarApagado);
        subirMinutos = findViewById(R.id.subirMinutos);
        bajarMinutos = findViewById(R.id.bajarMinutos);
        subirMinimo = findViewById(R.id.subirMinimo);
        bajarMinimo = findViewById(R.id.bajarMinimo);
        subirMaximo = findViewById(R.id.subirMaximo);
        bajarMaximo = findViewById(R.id.bajarMaximo);



        btnDesconectar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btSocket!=null)
                {
                    try {btSocket.close(); btAdapter.disable();}
                    catch (IOException e)
                    { Toast.makeText(getBaseContext(), "Error", Toast.LENGTH_SHORT).show();;}
                }
                finish();
            }
        });

        guardarDatos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyConexionBT.write("K");
                Toast.makeText(getBaseContext(), "Los datos fueron guardados", Toast.LENGTH_LONG).show();
            }
        });

        subirEncendido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyConexionBT.write("A");
            }
        });

        bajarEncendido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyConexionBT.write("B");
            }
        });

        subirApagado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyConexionBT.write("C");
            }
        });

        bajarApagado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyConexionBT.write("D");
            }
        });

        subirMinutos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyConexionBT.write("E");
            }
        });

        bajarMinutos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyConexionBT.write("F");
            }
        });

        subirMinimo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyConexionBT.write("G");
            }
        });

        bajarMinimo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyConexionBT.write("H");
            }
        });

        subirMaximo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyConexionBT.write("I");
            }
        });

        bajarMaximo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyConexionBT.write("J");
            }
        });
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException
    {
        //crea un conexion de salida segura para el dispositivo usando el servicio UUID
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        Intent intent = getIntent();
        address = intent.getStringExtra(DispositivosVinculados.EXTRA_DEVICE_ADDRESS);
        //Setea la direccion MAC
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try
        {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "La creacción del Socket fallo", Toast.LENGTH_LONG).show();
        }
        // Establece la conexión con el socket Bluetooth.
        try
        {
            btSocket.connect();
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {}
        }
        MyConexionBT = new ConnectedThread(btSocket);
        MyConexionBT.start();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        try
        { // Cuando se sale de la aplicación esta parte permite que no se deje abierto el socket
            btSocket.close();
            // Aquí debo deshabilitar el bluetooth del dispositivo
        } catch (IOException e2) {}
    }

    //Comprueba que el dispositivo Bluetooth
    //está disponible y solicita que se active si está desactivado
    private void VerificarEstadoBT() {

        if(btAdapter==null) {
            Toast.makeText(getBaseContext(), "El dispositivo no soporta bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (!btAdapter.isEnabled()) {
                //Solicitar al usuario que active Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    //Crea la clase que permite crear el evento de conexion
    private class ConnectedThread extends Thread
    {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket){
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }


        public void run()
        {
            byte[] byte_in = new byte[50]; // almacén de búfer para la secuencia
            int bytes; // bytes devueltos de read()
            // Se mantiene en modo escucha para determinar el ingreso de datos
            while (true) {
                try {
                    // Leer desde InputStream
                    bytes = mmInStream.read(byte_in); // Obtener el número de bytes y el mensaje en "buffer"
                    // char ch = (char) byte_in[0];
                    bluetoothIn.obtainMessage(RECIEVE_MESSAGE, bytes, -1, byte_in).sendToTarget(); // Enviar a la cola de mensajes Handler
                } catch (IOException e) {
                    break;
                }
            }
        }

        public void write(String message) {
            byte[] msgBuffer = message.getBytes();
            try {
                mmOutStream.write(msgBuffer);
            } catch (IOException e) {
                //si no es posible enviar datos se cierra la conexión
                Toast.makeText(getBaseContext(), "La Conexión fallo", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

}