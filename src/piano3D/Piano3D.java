package piano3D;

import customShapes.OneKey;
import customShapes.PianoKeys;
import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.*;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


import javax.sound.midi.*;
import java.io.*;
import java.util.*;

/**
 * Class represents a virtual piano in 3d scene
 *
 * @version 1.0
 * @autor Dvortsova Varvara
 */
public class Piano3D extends Application implements MetaEventListener {

    /**
     * Size of 3D scene
     */
    private double orgSceneX = 1000, orgSceneY = 500;
    private double orgTranslateX, orgTranslateY;

    /**
     * Number of note of 3 octave
     */
    private int octave = 60;

    /**
     * List of MIDI-channels
     */
    private MidiChannel[] mc;

    /**
     * Current MIDI-channel
     */
    private MidiChannel midiChannel;

    /**
     * Specifications of channel
     */
    private int velocity = 40, pressure = 0, bend = 8192, reverb = 64;

    /**
     * List of MIDI-instruments
     */
    private Instrument[] instruments;

    /**
     * MIDI synthesizer for sound reproduction
     */
    private Synthesizer synth;

    /**
     * Names of MIDI instruments
     */
    private final String names[] = {
            "Piano", "Chromatic Perc.", "Organ", "Guitar",
            "Bass", "Strings", "Ensemble", "Brass",
            "Reed", "Pipe", "Synth Lead", "Synth Pad",
            "Synth Effects", "Ethnic", "Percussive", "Sound Effects"};

    /**
     * Group for keys of piano
     */
    private SmartGroup groupPiano;

    /**
     * List of channel names
     */
    private ObservableList<String> channels;
    private ComboBox<String> channelsComboBox;

    private GridPane paneWithSetting;
    private GridPane paneWithInstruments;
    private TableView<TrackData> tableWithTracks;

    private Track track;
    private long startTime;
    private Sequence sequence;
    private Sequencer sequencer;
    boolean record = false;
    private int numOfChannel = 0;
    final int PROGRAM = 192;
    private static final int NOTEON = 144;
    private static final int NOTEOFF = 128;

    private CheckBox rdoMute, rdoSolo, rdoMono;
    private GridPane toolBarForRdoButton;
    private ScrollPane spinerWithInstruments;
    private Slider sliderBend, sliderReverb, sliderPressure, sliderVelocity;
    private Label valueBend, valueReverb, valuePressure, valueVelocity;
    private SubScene pianoSubScene;
    private Camera pianoCamera;
    private Button recordBtn;
    private VBox content;
    private Button instructionButton;
    private Button butnAllnotesOff;
    private BorderPane mainBorderPane;
    private Scene mainScene;
    private Label noteLabel;

    private Button recordB, playB, saveB;// for recording sound
    private Button btnSetStandartControls;
    private GridPane paneWithMainButtons;
    private GridPane paneWithComboBoxs;
    private GridPane paneWithSliders;
    private GridPane panelWithSettingControls;
    private GridPane panelSettingAndImage;
    private Image img;
    private ImageView imageView;
    TitledPane settingPane;

    //Variables to implement rotations of the keyboard around their axes
//    private double anchorX, anchorY;
//    private double anchorAnglX = 0;
//    private double anchorAnglY = 0;
    private final DoubleProperty anglelX = new SimpleDoubleProperty(0);
    private final DoubleProperty anglelY = new SimpleDoubleProperty(0);
    private int heightOfControls = 0;
    private int widhtOfControls = 200;

    private final String fileSource = "/source.txt";
    private final String fileKeyName = "/name_of_keys.txt";
    private final String textIntro = "/intro.txt";

    /**
     * Hash-table with keys of piano
     */
    Map<KeyCode, OneKey> hashMapWithKeys;

    @Override
    public void start(Stage primaryStage) throws MidiUnavailableException, InvalidMidiDataException, IOException {

        initMidiSystem();
        initKeysBoardOfPiano();

        sequence = new Sequence(Sequence.PPQ, 10);

        groupPiano = new SmartGroup(hashMapWithKeys.get(KeyCode.HOME).getMeshView());
        createPiano();
        ComboBox<String> octavesComboBox = createOctavesCombobox();
        channels = FXCollections.observableArrayList();
        channelsComboBox = createComboBoxChannel();
        createPaneWithMIDIChannelsModes();
        paneWithSetting = new GridPane();
        createPanelWithInstruments();
        createSliderWithValues();

        paneWithSetting.setHgap(10);
        paneWithSetting.setVgap(10);
        butnAllnotesOff = new Button("All Notes Off");
        butnAllnotesOff.setFont(new Font(14));

        butnAllnotesOff.setOnMouseClicked(e -> {
            midiChannel.allNotesOff();
            midiChannel.allSoundOff();
        });

        pianoSubScene = new SubScene(groupPiano, 1000, 500, true, SceneAntialiasing.BALANCED);
        pianoCamera = new PerspectiveCamera();
        pianoSubScene.setCamera(pianoCamera);

        instructionButton = createInstructionBtn();
        instructionButton.setFont(new Font(14));
        btnSetStandartControls = createResetSettingButton();

        paneWithMainButtons = new GridPane();
        paneWithMainButtons.setPadding(new Insets(5));
        paneWithMainButtons.setHgap(2);
        paneWithMainButtons.setVgap(2);


        instructionButton.setMaxSize(widhtOfControls, heightOfControls);
        btnSetStandartControls.setMaxSize(widhtOfControls, heightOfControls);
        btnSetStandartControls.setFont(new Font(14));
        octavesComboBox.setMaxSize(widhtOfControls, heightOfControls);
        channelsComboBox.setMaxSize(widhtOfControls, heightOfControls);
        butnAllnotesOff.setMaxSize(widhtOfControls, heightOfControls);
        butnAllnotesOff.setFont(new Font(14));


        paneWithMainButtons.add(instructionButton, 0, 0);
        paneWithMainButtons.add(btnSetStandartControls, 0, 1);
        paneWithMainButtons.add(butnAllnotesOff, 0, 2);


        paneWithComboBoxs = new GridPane();
        paneWithComboBoxs.setPadding(new Insets(5));
        paneWithComboBoxs.setHgap(2);
        paneWithComboBoxs.setVgap(2);
        paneWithComboBoxs.add(octavesComboBox, 0, 0);
        paneWithComboBoxs.add(channelsComboBox, 0, 1);

        recordBtn = createRecordBtn();
        paneWithComboBoxs.add(recordBtn, 0, 2);


        paneWithSliders = new GridPane();
        paneWithSliders.setPadding(new Insets(5));
        paneWithSliders.setHgap(2);
        paneWithSliders.setVgap(2);
        paneWithSliders.add(valueVelocity, 0, 0);
        paneWithSliders.add(sliderVelocity, 0, 1);

        paneWithSliders.add(valuePressure, 0, 2);
        paneWithSliders.add(sliderPressure, 0, 3);


        paneWithSliders.add(valueBend, 0, 4);
        paneWithSliders.add(sliderBend, 0, 5);

        panelWithSettingControls = new GridPane();

        panelWithSettingControls.add(toolBarForRdoButton, 0, 0);
        panelWithSettingControls.add(paneWithMainButtons, 1, 0);
        panelWithSettingControls.add(paneWithComboBoxs, 2, 0);
        panelWithSettingControls.add(paneWithSliders, 3, 0);
        noteLabel = new Label("  C1");
        noteLabel.setFont(new Font(90));
        panelWithSettingControls.add(noteLabel, 6, 0);

        addImagePiano();

        panelSettingAndImage = new GridPane();
        panelSettingAndImage.add(panelWithSettingControls, 0, 0);
        panelSettingAndImage.add(imageView, 0, 1);


        paneWithSetting.add(panelSettingAndImage, 0, 0);
        paneWithSetting.add(spinerWithInstruments, 2, 0);
        paneWithSetting.setPadding(new Insets(5));

        settingPane = new TitledPane();
        settingPane.setText("Setting Pane");
        content = new VBox();
        content.getChildren().add(paneWithSetting);
        settingPane.setContent(content);
        settingPane.setExpanded(true);


        mainBorderPane = new BorderPane();
        mainBorderPane.setTop(settingPane);
        groupPiano.translateXProperty().set(1000 / 2);
        groupPiano.translateYProperty().set(500 / 2);
        groupPiano.translateZProperty().set(-500);
        pianoSubScene.setFill(Color.SEAGREEN);
        mainBorderPane.setRight(pianoSubScene);
        mainBorderPane.setPrefSize(500, 400);

        mainScene = new Scene(mainBorderPane, orgSceneX, orgSceneY, true, SceneAntialiasing.BALANCED);
        addMouseScrolling(groupPiano);

        mainScene.setOnKeyPressed(event -> {
            if (event != null && event != null) {
                OneKey elem = hashMapWithKeys.get(event.getCode());
                if (elem != null) {
                    elem.setKeyPressed(setKeyPressed(elem.isKeyPressed(), octave + elem.getOctaveShift(), elem.getOctaveValue()));
                    noteLabel.setText(" " + elem.getNameOfKey());
                    rotateKey(elem.getMeshView());
                }
            }
        });

        mainScene.setOnKeyReleased(event -> {
            if (event.getCode() != null) {
                OneKey elem = hashMapWithKeys.get(event.getCode());
                if (elem != null) {
                    midiChannel.allNotesOff();
//                    midiChannel.noteOff(octave + elem.getOctaveShift());
                    elem.setKeyPressed(false);
                    elem.getMeshView().getTransforms().clear();
                    if (record) {
                        createShortEvent(NOTEOFF, octave + elem.getOctaveShift() + elem.getOctaveValue());
                    }
                }
            }
        });

        primaryStage.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event != null) {
                switch (event.getCode()) {

                    case MINUS:
                        groupPiano.rotateByX(-10);
                        break;
                    case DIGIT0:
                        groupPiano.rotateByX(10);
                        break;
                    case CONTEXT_MENU:
                        groupPiano.rotateByY(-10);
                        break;
                    case ALT_GRAPH:
                        groupPiano.rotateByY(10);
                        break;
                }
            }

        });

        primaryStage.setTitle("Piano3D");
        primaryStage.setScene(mainScene);


        primaryStage.widthProperty().addListener((observableValue, number, number2) -> {
            pianoSubScene.setWidth(primaryStage.getWidth());
            groupPiano.translateXProperty().set(primaryStage.getWidth() / 2 + 400);
            spinerWithInstruments.setPrefViewportWidth(primaryStage.getWidth() - 575);

        });

        primaryStage.heightProperty().addListener((observableValue, number, number2) -> {
            pianoSubScene.setHeight(primaryStage.getHeight());
            groupPiano.translateYProperty().set(primaryStage.getHeight() / 2 - 120);


        });
        primaryStage.show();
    }

    /**
     * Method for adding an image
     */
    private void addImagePiano() {
        // An image file on the hard drive.
        img = new Image(getClass().getResourceAsStream("/pianoImage.jpg"));
        imageView = new ImageView(img);
    }

    /**
     * Method for creating a panel with sliders for Velocity / Bend / Pressure
     */
    private void createSliderWithValues() {
        sliderBend = new Slider(0, 16383, bend);
        valueBend = new Label("Bend = " + bend);
        valueBend.setFont(new Font(14));
        sliderBend.valueProperty().addListener((changed, oldValue, newValue) -> {
            valueBend.setText("Bend = " + newValue.intValue());
            bend = newValue.intValue();
            midiChannel.setPitchBend(bend);
        });

        sliderPressure = new Slider(0, 127, pressure);
        valuePressure = new Label("Pressure = " + pressure);
        valuePressure.setFont(new Font(14));

        sliderPressure.valueProperty().addListener((changed, oldValue, newValue) -> {
            valuePressure.setText("Pressure = " + newValue.intValue());
            pressure = newValue.intValue();
            midiChannel.setChannelPressure(pressure);
        });

        sliderVelocity = new Slider(0, 127, velocity);
        valueVelocity = new Label("Velocity = " + velocity);
        valueVelocity.setFont(new Font(14));
        sliderVelocity.valueProperty().addListener((changed, oldValue, newValue) -> {
            valueVelocity.setText("Velocity = " + newValue.intValue());
            velocity = newValue.intValue();
        });

    }

    /**
     * Method for creating a panel with MIDI instruments
     */
    private void createPanelWithInstruments() {
        paneWithInstruments = new GridPane();
        int temp = 0;
        int instumentNumb = 0;
        int columnIndex = 0;
        for (int i = 0, j = 0; i < names.length; i++) {
            Rectangle border = new Rectangle(150, 30);
            border.setFill(Color.web("#757575"));
            Label lab = new Label(names[i]);
            lab.setFont(new Font(16));
            lab.setTextFill(Color.WHITE);
            StackPane pane = new StackPane();
            pane.getChildren().addAll(border, lab);
            paneWithInstruments.add(pane, columnIndex, 0);

            for (int k = 0; k < 8; k++) {
                TileInstrument tileInstrument = new TileInstrument(
                        instumentNumb,
                        instruments[instumentNumb++].getName(),
                        paneWithInstruments,
                        midiChannel,
                        instruments);
                paneWithInstruments.add(tileInstrument, columnIndex, k + 1);
            }

            columnIndex += 1;
        }

        paneWithInstruments.setPadding(new Insets(5));
        paneWithInstruments.setHgap(2);
        paneWithInstruments.setVgap(2);
        spinerWithInstruments = new ScrollPane(paneWithInstruments);
        spinerWithInstruments.setPrefViewportHeight(180);
        spinerWithInstruments.setPrefViewportWidth(450);
    }

    /**
     * Method for creating a piano keyboard
     */
    private void createPiano() {
        for (Map.Entry<KeyCode, OneKey> set : hashMapWithKeys.entrySet()) {
            if (set.getKey() != KeyCode.HOME) {
                groupPiano.getChildren().add(set.getValue().getMeshView());
            }
        }

        groupPiano.rotateByY(180);
        groupPiano.setCursor(Cursor.HAND);
        groupPiano.setOnMousePressed(groupOnMousePressedEventHandler);
        groupPiano.setOnMouseDragged(groupOnMouseDraggedEventHandler);
        groupPiano.getChildren().add(prepareLightSource());
    }

    /**
     * Method for creating a mode bar
     */
    private void createPaneWithMIDIChannelsModes() {

        rdoMute = new CheckBox("Mute");
        rdoMute.setFont(new Font(14));
        rdoMute.setOnAction(e -> {
            if (rdoMute.isSelected())
                midiChannel.setMute(true);
            else
                midiChannel.setMute(false);
        });

        rdoSolo = new CheckBox("Solo");
        rdoSolo.setFont(new Font(14));

        rdoSolo.setOnAction(e -> {
            if (rdoSolo.isSelected())
                midiChannel.setSolo(true);
            else
                midiChannel.setSolo(false);
        });

        rdoMono = new CheckBox("Mono");
        rdoMono.setFont(new Font(14));

        rdoMono.setOnAction(e -> {
            if (rdoMono.isSelected())
                midiChannel.setMono(true);
            else
                midiChannel.setMono(false);
        });

        toolBarForRdoButton = new GridPane();
        toolBarForRdoButton.setHgap(5);
        toolBarForRdoButton.setVgap(5);
        toolBarForRdoButton.add(rdoMute, 0, 1);
        toolBarForRdoButton.add(rdoSolo, 0, 2);
        toolBarForRdoButton.add(rdoMono, 0, 3);
    }


    /**
     * Method to initialize the MIDI system for the virtual piano
     *
     * @throws MidiUnavailableException
     * @throws InvalidMidiDataException
     */
    private void initMidiSystem() throws MidiUnavailableException, InvalidMidiDataException {
        sequencer = MidiSystem.getSequencer();
        sequence = new Sequence(Sequence.PPQ, 10);
        synth = MidiSystem.getSynthesizer();
        synth.open();
        mc = synth.getChannels();
        instruments = synth.getDefaultSoundbank().getInstruments();
        midiChannel = mc[numOfChannel];
        midiChannel.programChange(instruments[0].getPatch().getProgram());
        synth.loadInstrument(instruments[0]);
        midiChannel.setPitchBend(bend);
        midiChannel.setChannelPressure(pressure);
        midiChannel.controlChange(reverb, 0);
    }


    /**
     * Method that creates a list of octaves
     *
     * @return list of Octaves
     */
    private ComboBox<String> createOctavesCombobox() {
        ObservableList<String> octaves = FXCollections.observableArrayList(
                "Octave 1-4"
                , "Octave 2-5"
                , "Octave 3-6"
                , "Octave 4-7"
        );
        ComboBox<String> octavesComboBox = new ComboBox<>(octaves);


        octavesComboBox.setValue("Octave 3-6"); // sets the selected item to default
        octavesComboBox.setOnAction(event -> {
                    switch (octavesComboBox.getSelectionModel().getSelectedIndex()) {
                        case 0:
                            octave = 24;
                            break;
                        case 1:
                            octave = 36;
                            break;
                        case 2:
                            octave = 48;
                            break;
                        case 3:
                            octave = 60;
                            break;
                    }
                }
        );
        return octavesComboBox;
    }

    /**
     * Method for creating a channel list
     *
     * @return list of channels
     */
    private ComboBox<String> createComboBoxChannel() {
        ComboBox channelsComboBox = new ComboBox<>(channels);

        for (int i = 0; i < 16; i++) {
            channels.add("Channel " + i);
        }
        channelsComboBox.setValue("Channel 0"); // sets elem as default
        // gets selected item
        channelsComboBox.setOnAction(event -> {
                    bend = midiChannel.getPitchBend();
                    velocity = 40;
                    pressure = midiChannel.getChannelPressure();
                    numOfChannel = channelsComboBox.getSelectionModel().getSelectedIndex();
                    midiChannel = mc[numOfChannel];
                    midiChannel.programChange(instruments[0].getPatch().getProgram());
                    changeChannel(paneWithInstruments, midiChannel, 0);
                    sliderBend.setValue(bend);
                    sliderPressure.setValue(pressure);
                    sliderVelocity.setValue(velocity);
                    valueBend.setText("Bend = " + bend);
                    valuePressure.setText("Pressure = " + pressure);
                    valueVelocity.setText("Velocity = " + velocity);
                    midiChannel.setPitchBend(bend);
                    midiChannel.setChannelPressure(pressure);
                    midiChannel.controlChange(reverb, 0);
                }
        );
        return channelsComboBox;
    }

    /**
     * Method creating a button to reset the settings Velocity/Bend/Pressure
     *
     * @return button
     */
    private Button createResetSettingButton() {
        Button btn = new Button("Reset settings");
        btn.setOnMouseClicked(e -> {
            bend = 8192;
            velocity = 40;
            pressure = 0;

            sliderBend.setValue(bend);
            sliderPressure.setValue(pressure);
            sliderVelocity.setValue(velocity);
            valueBend.setText("Bend = " + bend);
            valuePressure.setText("Pressure = " + pressure);
            valueVelocity.setText("Velocity = " + velocity);
            midiChannel.setPitchBend(bend);
            midiChannel.setChannelPressure(pressure);
        });
        return btn;
    }

    //Method for rotating the keyboard around its axes
//    private void initMouseControl(SmartGroup groupPiano, Scene scene) {
//        Rotate xRotate;
//        Rotate yRotate;
//        groupPiano.getTransforms().addAll(
//                xRotate = new Rotate(0, Rotate.X_AXIS),
//                yRotate = new Rotate(0, Rotate.Y_AXIS)
//        );
//
//        xRotate.angleProperty().bind(anglelX);
//        yRotate.angleProperty().bind(anglelY);
//        scene.setOnMousePressed(x -> {
//            anchorX = x.getSceneX();
//            anchorY = x.getSceneY();
//            anchorAnglX = anglelX.get();
//            anchorAnglY = anglelY.get();
//        });
//        scene.setOnMouseDragged(e -> {
//            anglelX.set(anchorAnglX - (anchorAnglY - e.getSceneY()));
//            anglelY.set(anchorAnglY + (anchorAnglX - e.getSceneX()));
//
//        });
//    }

    /**
     * Method for adding piano keyboard scrolling along the OZ axis
     *
     * @param node
     */
    private void addMouseScrolling(Node node) {
        node.setOnScroll((ScrollEvent event) -> {
            double zoomFactor = 1.05;
            double deltaY = event.getDeltaY();
            if (deltaY < 0) {
                zoomFactor = 2.0 - zoomFactor;
            }
            if (!((groupPiano.getTranslateZ() * zoomFactor) < -1300))
                groupPiano.translateZProperty().set(groupPiano.getTranslateZ() * zoomFactor);

        });
    }

    /**
     * Method for creating light for a 3D keyboard scene
     *
     * @return node
     */
    private Node prepareLightSource() {

        PointLight pointLight = new PointLight();
        pointLight.setColor(Color.WHITE);
        pointLight.getTransforms().add(new Translate(100, -300, 700));
        return pointLight;

    }

    /**
     * Method for initializing keys from a file
     */
    private void initKeysBoardOfPiano() throws IOException {

        hashMapWithKeys = new HashMap();

        String lineFromSourceFile = "";
        String lineFromNameFile = "";

        BufferedReader readSource = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(fileSource)));
        BufferedReader readNames = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(fileKeyName)));

        List<String> linesSource = new ArrayList<String>();
        List<String> linesName = new ArrayList<String>();

        while ((lineFromSourceFile = readSource.readLine()) != null && (lineFromNameFile = readNames.readLine()) != null) {
            linesSource.add(lineFromSourceFile);
            linesName.add(lineFromNameFile);
        }
        readSource.close();
        readNames.close();

        for (int i = 0; i < linesName.size(); i++) {

            List<String> tempSource = new ArrayList<String>(Arrays.asList(linesSource.get(i).split(" ")));
            List<String> tempName = new ArrayList<String>(Arrays.asList(linesName.get(i).split(" ")));
            String nameOfKey = tempName.get(0);
            String typeOfKey = tempName.get(1);
            String codeKey = tempName.get(2).replace('_', ' ');
            MeshView currentMesh = PianoKeys.getKeyByType(PianoKeys.KeyType.WHITE);

            switch (typeOfKey) {
                case "w":
                    currentMesh = PianoKeys.getKeyByType(PianoKeys.KeyType.WHITE);
                    break;
                case "b":
                    currentMesh = PianoKeys.getKeyByType(PianoKeys.KeyType.BLACK);
                    break;
                case "t":
                    currentMesh = PianoKeys.getKeyByType(PianoKeys.KeyType.T);
                    break;
                case "gl":
                    currentMesh = PianoKeys.getKeyByType(PianoKeys.KeyType.GL);
                    break;
                case "gr":
                    currentMesh = PianoKeys.getKeyByType(PianoKeys.KeyType.GR);
                    break;
                case "tr":
                    currentMesh = PianoKeys.getKeyByType(PianoKeys.KeyType.TR);
                    break;
                case "tl":
                    currentMesh = PianoKeys.getKeyByType(PianoKeys.KeyType.TL);
                    break;
            }
            int shift = Integer.parseInt(tempSource.get(0))
                    + Integer.parseInt(tempSource.get(1))
                    + Integer.parseInt(tempSource.get(2));
            int value = Integer.parseInt(tempSource.get(3));
            double x = Double.parseDouble(tempSource.get(4));
            double z = Double.parseDouble(tempSource.get(6));

            OneKey key = new OneKey(
                    currentMesh,
                    nameOfKey,
                    KeyCode.getKeyCode(codeKey),
                    shift, value, x, z);

            key.getMeshView().addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
                noteLabel.setText(" " + key.getNameOfKey());
                key.setKeyPressed(setKeyPressed(key.isKeyPressed(), octave + key.getOctaveShift(), key.getOctaveValue()));
                rotateKey(key.getMeshView());
            });

            key.getMeshView().addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
                key.getMeshView().getTransforms().clear();
                key.setKeyPressed(false);
                midiChannel.allNotesOff();
                if (record) {
                    createShortEvent(NOTEOFF, octave + key.getOctaveShift() + key.getOctaveValue());
                }
            });
            hashMapWithKeys.put(KeyCode.getKeyCode(codeKey), key);
        }
        hashMapWithKeys.size();

    }


    /**
     * Method for rotating a key along the OX axis
     *
     * @param mesh
     */
    void rotateKey(MeshView mesh) {
        Rotate r = new Rotate(-5, Rotate.X_AXIS);
        Transform t = new Rotate();
        t = t.createConcatenation(r);
        mesh.getTransforms().clear();
        mesh.getTransforms().addAll(t);
    }

    /**
     * The method turn on sound on the midi channel
     *
     * @param key
     * @param octava the note that will sound
     * @param value  offset
     * @return
     */
    private boolean setKeyPressed(boolean key, int octava, int value) {
        if (!key) {
            midiChannel.noteOn(octava + value, velocity);
            if (record) {
                createShortEvent(NOTEON, octava + value);
            }
        } else {
            midiChannel.noteOff(octava + value, velocity);
            if (record) {
                createShortEvent(NOTEOFF, octava + value);
            }
        }
        return true;
    }

    /**
     * Event for keyboard movement OnMousePressed
     * using a computer mouse or touchpad
     */
    EventHandler<MouseEvent> groupOnMousePressedEventHandler =
            new EventHandler<>() {

                @Override
                public void handle(MouseEvent t) {
                    if (t.getButton() == MouseButton.SECONDARY) {
                        midiChannel.allNotesOff();
                        orgSceneX = t.getSceneX();
                        orgSceneY = t.getSceneY();
                        orgTranslateX = ((SmartGroup) (t.getSource())).getTranslateX();
                        orgTranslateY = ((SmartGroup) (t.getSource())).getTranslateY();
                    }

                }
            };

    /**
     * Event for moving the keyboard OnMouseDragged
     * using a computer mouse or touchpad
     */
    EventHandler<MouseEvent> groupOnMouseDraggedEventHandler =
            new EventHandler<>() {

                @Override
                public void handle(MouseEvent t) {
                    if (t.getButton() == MouseButton.SECONDARY) {
                        midiChannel.allNotesOff();
                        double offsetX = t.getSceneX() - orgSceneX;
                        double offsetY = t.getSceneY() - orgSceneY;
                        double newTranslateX = orgTranslateX + offsetX;
                        double newTranslateY = orgTranslateY + offsetY;
                        ((SmartGroup) (t.getSource())).setTranslateX(newTranslateX);
                        ((SmartGroup) (t.getSource())).setTranslateY(newTranslateY);
                    }

                }
            };

    /**
     * Method to change the current channel and
     * drawing a table with tools
     *
     * @param pane
     * @param channel
     * @param instrument
     */
    private void changeChannel(GridPane pane, MidiChannel channel, int instrument) {
        ObservableList<Node> nodes = pane.getChildren();
        for (Node node : nodes) {
            if (node.getClass() == TileInstrument.class) {
                ((TileInstrument) node).setChannel(channel);
                if (((TileInstrument) node).getNumbOfInstrument() == 0)
                    ((TileInstrument) node).getTileBorder().setFill(Color.PINK);
                else ((TileInstrument) node).getTileBorder().setFill(Color.web("#e5e5e5"));
            }
        }
    }

    @Override
    public void meta(MetaMessage metaMessage) {
        if (metaMessage.getType() == 47) {  // 47 is end of track
            playB.setDisable(true);
            recordB.setDisable(false);
        }
    }


    public class TrackData {
        private SimpleIntegerProperty chanNum;
        private SimpleStringProperty name;
        private Track track;

        TrackData(int chanNum, String name, Track track) {
            this.chanNum = new SimpleIntegerProperty(chanNum);
            this.name = new SimpleStringProperty(name);
            this.track = track;
        }

        public String getName() {
            return name.get();
        }

        public void setName(String value) {
            name.set(value);
        }

        public int getChanNum() {
            return chanNum.get();
        }

        public void setChanNum(int value) {
            chanNum.set(value);
        }

        public Track getTrack() {
            return track;
        }
    } // End class TrackData

    private void createShortEvent(int type, int num) {
        ShortMessage message = new ShortMessage();
        try {
            long millis = System.currentTimeMillis() - startTime;
            long tick = millis * sequence.getResolution() / 500;
            message.setMessage(type + numOfChannel, num, velocity);
            MidiEvent event = new MidiEvent(message, tick);
            track.add(event);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * The method creates a button to create a window for
     * sequence records
     *
     * @return button
     */
    private Button createRecordBtn() {
        Button recordBtn = new Button("Record");
        recordBtn.setMaxSize(widhtOfControls, heightOfControls);
        recordBtn.setFont(new Font(14));
        recordBtn.setTextFill(Color.WHITE);
        recordBtn.setBackground(new Background(new BackgroundFill(Color.web("#757575"), null, null)));
        ObservableList<TrackData> trackDate = FXCollections.observableArrayList();
        tableWithTracks = new TableView<>(trackDate);
        tableWithTracks.setPrefWidth(250);
        tableWithTracks.setPrefHeight(200);
        TableColumn<TrackData, Integer> channelColumn = new TableColumn<TrackData, Integer>("Channel");
        channelColumn.setCellValueFactory(new PropertyValueFactory<>("chanNum"));
        tableWithTracks.getColumns().add(channelColumn);
        TableColumn<TrackData, String> nameColumn = new TableColumn<TrackData, String>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        tableWithTracks.getColumns().add(nameColumn);

        TableView.TableViewSelectionModel<TrackData> selectionModel = tableWithTracks.getSelectionModel();
        selectionModel.selectedItemProperty().addListener((observable, oldValue, newValue) -> {
//                if(newValue != null) System.out.println("Selected: " + newValue.getName());
        });
        sequencer.addMetaEventListener(this);

        recordBtn.setOnMouseClicked(event -> {
            BorderPane root;
            root = new BorderPane() {
            };
            recordB = new Button("Record");
            playB = new Button("Play");
            saveB = new Button("Save");

            GridPane menu = new GridPane();
            menu.add(saveB, 0, 1);
            menu.add(playB, 1, 1);
            menu.add(recordB, 2, 1);

            menu.setVgap(10);
            root.setLeft(menu);
            root.setBottom(tableWithTracks);
            Stage stage = new Stage();
            stage.setResizable(false);
            stage.setTitle("Record Panel");
            Scene recordPane = new Scene(root, 170, 170);
            stage.setScene(recordPane);
            stage.show();
            playB.setDisable(true);
            saveB.setDisable(true);
            recordB.setOnMouseClicked(e -> {
                if (recordB.getText().startsWith("Stop")) {
                    record = false;
                    playB.setDisable(false);
                    saveB.setDisable(false);
                }
                if (recordB.getText().startsWith("Record")) {
                    record = true;
                    try {
                        sequence = new Sequence(Sequence.PPQ, 10);
                    } catch (InvalidMidiDataException ex) {
                        ex.printStackTrace();
                    }
                }
                if (record) {
                    track = sequence.createTrack();
                    startTime = System.currentTimeMillis();

                    // add a program change right at the beginning of
                    // the track for the current instrument
                    createShortEvent(PROGRAM, midiChannel.getProgram());

                    recordB.setText("Stop");
                    playB.setDisable(true);
                    saveB.setDisable(true);
                } else {
                    String name = null;
                    if (instruments != null) {
                        name = instruments[midiChannel.getProgram()].getName();
                    } else {
                        name = Integer.toString(midiChannel.getProgram());
                    }
                    trackDate.add(new TrackData(numOfChannel, name, track));
                    recordB.setText("Record");
                    playB.setDisable(false);
                    saveB.setDisable(false);
                }


            });
            playB.setOnMouseClicked(e -> {
                if (playB.getText().startsWith("Play")) {
                    try {
                        sequencer.open();
                        sequencer.setSequence(sequence);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    sequencer.start();
                    recordB.setDisable(true);
                } else {
                    sequencer.stop();
                    playB.setText("Play");
                    recordB.setDisable(false);
                }

            });
            saveB.setOnMouseClicked(e -> {
                try {
                    FileChooser fileChooser = new FileChooser();
                    fileChooser.getExtensionFilters().addAll(
                            new FileChooser.ExtensionFilter("Audio Files", "*.wav", "*.midi"),
                            new FileChooser.ExtensionFilter("All Files", "*.*")
                    );
                    File selectedFile = fileChooser.showSaveDialog(stage);
                    if (selectedFile != null) {
                        saveMidiFile(selectedFile);
                    }

                } catch (SecurityException ex) {
                    ex.printStackTrace();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

        });

        return recordBtn;
    }

    /**
     * Method creates a window with information on
     * using the piano
     *
     * @return
     */
    private Button createInstructionBtn() {
        Button instruction = new Button("Instruction");
        instruction.setOnMouseClicked(e -> {
            BorderPane root;
            root = new BorderPane();
            Reader inputStreamReader;
            StringBuffer sb = new StringBuffer();

            try {
                inputStreamReader = new InputStreamReader(getClass().getResourceAsStream(textIntro));
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String str;
                while (true) {
                    if (!((str = reader.readLine()) != null)) break;
                    sb.append(str + "\n");
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            Label intro = new Label(sb.toString());


            intro.setFont(new Font(14)); // set to Label
            intro.setWrapText(true);
            Scene recordPane = new Scene(root, 800, 500);
            ScrollPane spiner = new ScrollPane(intro);
            spiner.setPrefViewportHeight(recordPane.getHeight());
            spiner.setPrefViewportWidth(recordPane.getWidth());
            root.setLeft(spiner);

            Stage stage = new Stage();
            stage.widthProperty().addListener((observableValue, number, number2) -> spiner.setPrefViewportWidth(stage.getWidth()));
            stage.heightProperty().addListener((observableValue, number, number2) -> spiner.setPrefViewportHeight(stage.getHeight()));
            stage.setTitle("Instruction");
            stage.setScene(recordPane);
            stage.show();
        });
        return instruction;
    }

    /**
     * Method for saving midi file
     *
     * @param file
     */
    public void saveMidiFile(File file) {
        try {
            int[] fileTypes = MidiSystem.getMidiFileTypes(sequence);
            if (fileTypes.length == 0) {
                System.out.println("Can't save sequence");
            } else {
                if (MidiSystem.write(sequence, fileTypes[0], file) == -1) {
                    throw new IOException("Problems writing to file");
                }
            }
        } catch (SecurityException ex) {
//            JavaSound.showInfoDialog();
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Class for a musical instrument table cell
     */
    class TileInstrument extends StackPane {

        private Rectangle border;
        private Label label;
        private MidiChannel channel;
        private Instrument[] instruments;
        private GridPane pane;


        public TileInstrument(int num, String name, GridPane pane, MidiChannel channel, Instrument[] instruments) {
            border = new Rectangle(150, 25);
            border.setFill(Color.web("#e5e5e5"));
            border.setOnMouseClicked(e -> {
                border.setFill(Color.BLUE);
            });
            this.pane = pane;
            this.instruments = instruments;
            this.label = new Label(name);
            this.numbOfInstrument = num;
            this.channel = channel;

            setOnMouseClicked(e -> {
                this.channel.allNotesOff();
                this.border.setFill(Color.PINK);

                synth.loadInstrument(instruments[numbOfInstrument]);
                if (record) {
                    createShortEvent(PROGRAM, numbOfInstrument);
                }
                this.channel.programChange(this.instruments[numbOfInstrument].getPatch().getProgram());
                redrawGrid(this.pane);
                sliderBend.setValue(midiChannel.getPitchBend());
                sliderPressure.setValue(midiChannel.getChannelPressure());
                sliderVelocity.setValue(64);
                bend = midiChannel.getPitchBend();
                velocity = 40;
                pressure = midiChannel.getChannelPressure();
                valueBend.setText("Bend = " + midiChannel.getPitchBend());
                valuePressure.setText("Pressure = " + midiChannel.getChannelPressure());
                valueVelocity.setText("Velocity = " + velocity);

            });
            getChildren().addAll(border, label);
        }

        public int getNumbOfInstrument() {
            return numbOfInstrument;
        }

        private int numbOfInstrument;

        public void setChannel(MidiChannel channel) {
            this.channel = channel;
        }

        public Rectangle getTileBorder() {
            return border;
        }


        /**
         * Method for redrawing the toolbar
         *
         * @param pane -grid panel with tools
         */
        private void redrawGrid(GridPane pane) {
            ObservableList<Node> nodes = pane.getChildren();
            for (Node node : nodes) {
                if (node.getClass() == TileInstrument.class && node != this) {
                    ((TileInstrument) node).border.setFill(Color.web("#e5e5e5"));
                }
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}
