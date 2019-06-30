package com.cursoandroid.nearbyconnections;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;


import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION=1;
    // Consejo: utiliza como SERVICE_ID el nombre de tu paquete
    private static final String SERVICE_ID = "com.cursoandroid.nearbyconnections";
    private static final String TAG = "Mobile:";
    Button botonLED,bScan,bConnect,bOn,bOff,bDisconnect;
    TextView textview;
    //**********************************
    private String thisEndpointId;
    //**********************************


    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textview = (TextView) findViewById(R.id.textView1);
        botonLED = (Button) findViewById(R.id.buttonLED);
        bScan = (Button) findViewById(R.id.b_scan);
        bConnect = (Button) findViewById(R.id.b_connect);
        bOn = (Button) findViewById(R.id.b_on);
        bOff = (Button) findViewById(R.id.b_off);
        bDisconnect = (Button) findViewById(R.id.b_disconnect);

        botonLED.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i(TAG, "Boton presionado");
                //startDiscovery();
                sendData(thisEndpointId, "WIFI");
                textview.setText("Configurando WiFi...");
            }
        });

        bConnect.setEnabled(false);
        bOn.setEnabled(false);
        bOff.setEnabled(false);
        bDisconnect.setEnabled(false);

        //ACCIONES AL PULSAR ************************************
        bScan.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i(TAG, "Botón Scan presionado");
                //Buscar
                startDiscovery();
                textview.setText("Buscando...");
            }
        });

        bConnect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i(TAG, "Botón Connect presionado");
                //Conectar
                conectar();
                textview.setText("Conectando...");
            }
        });

        bOn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i(TAG, "Presionado ON");
                //Encender LED
                sendData(thisEndpointId, "SWITCH ON");
                textview.setText("Encendiendo LED...");
            }
        });

        bOff.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i(TAG, "Presionado OFF");
                //Apagar LED
                sendData(thisEndpointId, "SWITCH OFF");
                textview.setText("Apagando LED...");
            }
        });

        bDisconnect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i(TAG, "Presionado Disconnect");
                //Desconectar
                Nearby.getConnectionsClient(getApplicationContext()).disconnectFromEndpoint(thisEndpointId);
                textview.setText("Desconectando...");
            }
        });
        //************************************************************
        
        // Comprobación de permisos peligrosos. Se pide permiso al usuario en tiempo de ejecución
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
        }
    }

    // Gestión de permisos
    @Override  public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Permisos concedidos");
                } else {
                    Log.i(TAG, "Permisos denegados");
                    textview.setText("Debe aceptar los permisos para comenzar");
                    botonLED.setEnabled(false);
                }
                return;
            }
        }
    }

    private void startDiscovery() {
        Nearby.getConnectionsClient(this).startDiscovery(SERVICE_ID,
                mEndpointDiscoveryCallback, new DiscoveryOptions(Strategy.P2P_STAR))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override public void onSuccess(Void unusedResult) {
                        Log.i(TAG, "Estamos en modo descubrimiento!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Modo descubrimiento no iniciado.", e);
                    }
                });
    }

    private void stopDiscovery() {
        Nearby.getConnectionsClient(this).stopDiscovery();
        Log.i(TAG, "Se ha detenido el modo descubrimiento.");
    }

    //Gestiona el proceso de descubrimiento de anunciantes y la estrategia a seguir.
    //Se invoca cada vez que detecta un anunciante o que desaparece uno
    private final EndpointDiscoveryCallback mEndpointDiscoveryCallback =     new EndpointDiscoveryCallback() {

        //Se lanza cuando detecta un anunciante
        @Override public void onEndpointFound(String endpointId, DiscoveredEndpointInfo discoveredEndpointInfo) {
            Log.i(TAG, "Descubierto dispositivo con Id: " + endpointId);
            thisEndpointId = endpointId;
            String discoveredPointName = "Descubierto: " +discoveredEndpointInfo.getEndpointName();
            textview.setText(discoveredPointName);
            stopDiscovery();
            bConnect.setEnabled(true);
            // Iniciamos la conexión con al anunciante "Nearby LED"
            /*Log.i(TAG, "Conectando...");
            Nearby.getConnectionsClient(getApplicationContext())
                    .requestConnection("Nearby LED",endpointId,mConnectionLifecycleCallback)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override public void onSuccess(Void unusedResult) {
                            Log.i(TAG, "Solicitud lanzada, falta que ambos lados acepten");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Error en solicitud de conexión", e);
                            textview.setText("Desconectado");
                        }
                    });*/
        }

        //Se lanza cuando se pierde un anunciante
        @Override public void onEndpointLost(String endpointId) {}
    };

    //Se invoca durante el proceso de conexión con el anunciante
    private final ConnectionLifecycleCallback mConnectionLifecycleCallback =
            new ConnectionLifecycleCallback() {

                @Override public void onConnectionInitiated(
                        String endpointId, ConnectionInfo connectionInfo) {
                    // Aceptamos la conexión automáticamente en ambos lados.
                    Log.i(TAG, "Aceptando conexión entrante sin autenticación");
                    Nearby.getConnectionsClient(getApplicationContext())
                            .acceptConnection(endpointId, mPayloadCallback);
                }

                //Se invoca cuando el proceso de conexión ha finalizado
                @Override public void onConnectionResult(String endpointId,
                                                         ConnectionResolution result) {
                    switch (result.getStatus().getStatusCode()) {
                        case ConnectionsStatusCodes.STATUS_OK:
                            Log.i(TAG, "Estamos conectados!");
                            textview.setText("Conectado");
                            bScan.setEnabled(false);
                            bConnect.setEnabled(false);
                            bOn.setEnabled(true);
                            bOff.setEnabled(true);
                            bDisconnect.setEnabled(true);
                            //La conexión es correcta. sendData() para enviar el mensaje “ACCION” a la RPi3
                            //sendData(endpointId, "SWITCH");  //********************************************
                            break;
                        case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                            Log.i(TAG, "Conexión rechazada por uno o ambos lados");
                            textview.setText("Desconectado");
                            break;
                        case ConnectionsStatusCodes.STATUS_ERROR:
                            Log.i(TAG, "Conexión perdida antes de poder ser aceptada");
                            textview.setText("Desconectado");
                            break;
                    }
                }



                @Override public void onDisconnected(String endpointId) {
                    Log.i(TAG, "Desconexión del endpoint, no se pueden intercambiar más datos.");
                    textview.setText("Desconectado");
                    bOn.setEnabled(false);
                    bOff.setEnabled(false);
                    bDisconnect.setEnabled(false);
                    bConnect.setEnabled(true);
                    bScan.setEnabled(true);
                }
            };

    //Se invoca cuando se reciben datos del anunciante
    private final PayloadCallback mPayloadCallback = new PayloadCallback() {
        // En este ejemplo, el móvil no recibirá transmisiones de la RP3
        @Override public void onPayloadReceived(String endpointId, Payload payload) {
            // Payload recibido
        }

        @Override public void onPayloadTransferUpdate(String endpointId,PayloadTransferUpdate update) {
            // Actualizaciones sobre el proceso de transferencia
        }
    };

    private void sendData(String endpointId, String mensaje) {
        textview.setText("Transfiriendo...");
        Payload data = null;
        try {
            data = Payload.fromBytes(mensaje.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Error en la codificación del mensaje.", e);
        }
        Nearby.getConnectionsClient(this).sendPayload(endpointId, data);
        Log.i(TAG, "Mensaje enviado.");
    }

    private void conectar(){
        // Iniciamos la conexión con al anunciante "Nearby LED"
        Log.i(TAG, "Conectando...");

        Nearby.getConnectionsClient(getApplicationContext())
                //.requestConnection("Nearby LED",thisEndpointId,mConnectionLifecycleCallback)
                .requestConnection(thisEndpointId,thisEndpointId,mConnectionLifecycleCallback)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override public void onSuccess(Void unusedResult) {
                        Log.i(TAG, "Solicitud lanzada, falta que ambos lados acepten");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error en solicitud de conexión", e);
                        textview.setText("Desconectado");
                    }
                });
    }
}
