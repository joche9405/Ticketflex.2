package com.tu_paquete.ticketflex.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.tu_paquete.ticketflex.Model.FuncionEvento;
import com.tu_paquete.ticketflex.Model.PerfilUsuario;
import com.tu_paquete.ticketflex.Model.PrediccionResultado;
import com.tu_paquete.ticketflex.Service.dto.NuevaPersonaRequest;
import com.tu_paquete.ticketflex.Service.dto.PrediccionMasivaResultado;
import com.tu_paquete.ticketflex.Service.dto.ResultadoPrediccion;
import com.tu_paquete.ticketflex.repository.jpa.FuncionEventoRepository;
import com.tu_paquete.ticketflex.repository.jpa.PerfilUsuarioRepository;

import weka.classifiers.Classifier;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import java.util.List;

import weka.core.Attribute;
import java.util.ArrayList;

import java.text.DecimalFormat;
import java.util.logging.Logger;

@Service
public class PrediccionEventoService {

    private static final Logger LOGGER = Logger.getLogger(PrediccionEventoService.class.getName());
    private Classifier classifier;
    private Instances dataStructure;

    @Autowired
    private PerfilUsuarioRepository perfilUsuarioRepository;

    @Autowired
    private FuncionEventoRepository funcionEventoRepository;

    @Autowired
    public PrediccionEventoService() {
        try {
            ClassPathResource modelResource = new ClassPathResource("compra_boleta_con_descuento.model");
            classifier = (Classifier) weka.core.SerializationHelper.read(modelResource.getInputStream());
            LOGGER.info("Modelo compra_boleta cargado exitosamente.");

            ClassPathResource arffResource = new ClassPathResource("compra_boleta_con_descuento_10k.arff");
            DataSource source = new DataSource(arffResource.getInputStream());
            dataStructure = source.getDataSet();
            dataStructure.setClassIndex(dataStructure.numAttributes() - 1);
            LOGGER.info("Estructura compra_boleta.arff cargada exitosamente.");
        } catch (Exception e) {
            LOGGER.severe("Error al inicializar PrediccionEventoService: " + e.getMessage());
            throw new RuntimeException("No se pudo inicializar el servicio de predicción de eventos", e);
        }
    }

    public ResultadoPrediccion predecirAsistenciaEvento(Long idUsuario, Long idFuncion) throws Exception {
        PerfilUsuario perfilUsuario = perfilUsuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new Exception("Perfil de usuario no encontrado con ID: " + idUsuario));
        FuncionEvento funcionEvento = funcionEventoRepository.findById(idFuncion)
                .orElseThrow(() -> new Exception("Función de evento no encontrada con ID: " + idFuncion));

        Instance instance = crearInstanciaWeka(perfilUsuario);
        double predictionValue = classifier.classifyInstance(instance);
        String prediction = dataStructure.classAttribute().value((int) predictionValue);
        double[] probabilities = classifier.distributionForInstance(instance);
        double confidence = probabilities[(int) predictionValue];
        DecimalFormat df = new DecimalFormat("#.#%");
        String confidencePercentage = df.format(confidence);

        return new ResultadoPrediccion(prediction, confidencePercentage);
    }

    public List<PrediccionMasivaResultado> predecirAsistenciaEventoMasivo(Long idEvento) throws Exception {
        FuncionEvento funcionEvento = funcionEventoRepository.findById(idEvento)
                .orElseThrow(() -> new Exception("Función de evento no encontrada con ID: " + idEvento));
        List<PerfilUsuario> usuarios = perfilUsuarioRepository.findAll();
        List<PrediccionMasivaResultado> resultados = new ArrayList<>();
        DecimalFormat df = new DecimalFormat("#.#%");

        for (PerfilUsuario usuario : usuarios) {
            Instance instance = crearInstanciaWeka(usuario);
            double predictionValue = classifier.classifyInstance(instance);
            String prediccion = dataStructure.classAttribute().value((int) predictionValue);
            double[] probabilities = classifier.distributionForInstance(instance);
            double confianza = probabilities[(int) predictionValue];
            String confianzaPercentage = df.format(confianza);

            resultados.add(new PrediccionMasivaResultado(
                    usuario.getIdPerfil(),
                    usuario.getEdadUsuario(),
                    usuario.getGeneroUsuario(),
                    prediccion,
                    confianzaPercentage,
                    categorizarHistorial(usuario.getHistorialComprasTotal()),
                    usuario.getInteresPrincipal()));
        }
        return resultados;
    }

    public FuncionEvento obtenerEventoPorId(Long idEvento) {
        return funcionEventoRepository.findById(idEvento).orElse(null);
    }

    public PrediccionResultado predecirAsistenciaNuevaPersona(NuevaPersonaRequest personaRequest) throws Exception {
        Instance nuevaInstancia = crearInstanciaWekaNuevaPersona(personaRequest);
        double resultadoPrediccion = classifier.classifyInstance(nuevaInstancia);
        double[] probabilidades = classifier.distributionForInstance(nuevaInstancia);
        String clasePredicha = dataStructure.classAttribute().value((int) resultadoPrediccion);
        double confianza = probabilidades[(int) resultadoPrediccion] * 100;

        return new PrediccionResultado(clasePredicha.equals("1") ? "Asistirá" : "No Asistirá",
                String.format("%.1f%%", confianza));
    }

    private Instance crearInstanciaWeka(PerfilUsuario usuario) {
        Instance instance = new DenseInstance(dataStructure.numAttributes());
        instance.setDataset(dataStructure);

        instance.setValue(dataStructure.attribute("ID_Usuario"), usuario.getIdPerfil().doubleValue());
        instance.setValue(dataStructure.attribute("Edad"), usuario.getEdadUsuario());
        instance.setValue(dataStructure.attribute("Genero"), usuario.getGeneroUsuario());
        instance.setValue(dataStructure.attribute("Historial_Compras"),
                categorizarHistorial(usuario.getHistorialComprasTotal()));
        instance.setValue(dataStructure.attribute("Frecuencia_Visitas_Sitio"), usuario.getFrecuenciaVisitas());
        instance.setValue(dataStructure.attribute("Tiempo_Navegacion_Promedio"), usuario.getTiempoPromedioNavegacion());
        instance.setValue(dataStructure.attribute("Interes_Categoria"), usuario.getInteresPrincipal());
        instance.setValue(dataStructure.attribute("Dispositivo_Acceso"), usuario.getDispositivoPredilecto());
        instance.setValue(dataStructure.attribute("Recibio_Notificacion"),
                usuario.getRecibeNotificaciones() ? "1" : "0");
        instance.setValue(dataStructure.attribute("Uso_Descuento_Previo"), usuario.getUsoDescuentoPrevio() ? "1" : "0");

        return instance;
    }

    private Instance crearInstanciaWekaNuevaPersona(NuevaPersonaRequest personaRequest) {
        Instance instance = new DenseInstance(dataStructure.numAttributes());
        instance.setDataset(dataStructure);

        instance.setValue(dataStructure.attribute("ID_Usuario"), 0); // Valor dummy
        instance.setValue(dataStructure.attribute("Edad"), personaRequest.getEdadUsuario());
        instance.setValue(dataStructure.attribute("Genero"), personaRequest.getGeneroUsuario());
        instance.setValue(dataStructure.attribute("Historial_Compras"),
                categorizarHistorial(personaRequest.getHistorialComprasTotal()));
        instance.setValue(dataStructure.attribute("Frecuencia_Visitas_Sitio"), personaRequest.getFrecuenciaVisitas());
        instance.setValue(dataStructure.attribute("Tiempo_Navegacion_Promedio"), personaRequest.getTiempoNavegacion());
        instance.setValue(dataStructure.attribute("Interes_Categoria"), personaRequest.getInteresPrincipal());
        instance.setValue(dataStructure.attribute("Dispositivo_Acceso"), personaRequest.getDispositivoPredilecto());
        instance.setValue(dataStructure.attribute("Recibio_Notificacion"),
                String.valueOf(personaRequest.getRecibeNotificaciones()));
        instance.setValue(dataStructure.attribute("Uso_Descuento_Previo"),
                String.valueOf(personaRequest.getUsoDescuentoPrevio()));

        return instance;
    }

    private String categorizarHistorial(int historial) {
        if (historial < 4) {
            return "Bajo";
        } else if (historial < 8) {
            return "Medio";
        } else {
            return "Alto";
        }
    }
}