/*
 * CSP_GUIView.java
 */

package csp_gui;

import csp.ByRef;
import csp.Chromosome;
import csp.CspProcess;
import csp.Draw;
import csp.ExternalData;
import csp.MyException;
import csp.UserInput;
import java.awt.Color;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.Scanner;
import java.util.StringTokenizer;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import org.jdesktop.application.Application;
//import user_code.GraphColoring;
//import user_code.MapColoring;
//import user_code.Nqueen;
//import user_code.NqueenII;
//import user_code.NqueenII;
//import user_code.TimeTableData;

/**
 * The application's main frame.
 */
public class CSP_GUIView extends FrameView {

    class StartProcessThread extends Thread{

        @Override
        public void run() {     
            PrintWriter resultToFile;
            PrintWriter chromes;
            ByRef nextPrefSuggestion = new ByRef("????");
  
            progressBar.setMinimum(0);
            progressBar.setMaximum ( 100);

            try {
                Chromosome.totalEvals = 0;

                cspProcess_.start(progressBar, chkSaveChromes.isSelected(), nextPrefSuggestion, draw);
                resultToFile = new PrintWriter(new FileWriter(new File(".").getCanonicalPath() + "/output.txt")); 
                chromes = new PrintWriter(new FileWriter(new File(".").getCanonicalPath() + "/chromosomes.txt")); 
                resultToFile.println("\n\n--------------------------------------------------");
                resultToFile.println("Final Solution [" +cspProcess_.getSolution().size() +"]" );
                resultToFile.println(cspProcess_.getSolution());
                if(chkSaveChromes.isSelected())
                    chromes.print(cspProcess_.printChromeValues());
                else
                    chromes.print("");
                chromes.close();
                resultToFile.close(); 
                lblPrefSuggestion.setText("(Suggestion: "+(String)nextPrefSuggestion.getVal()+")");
            } catch (MyException me) {
                me.showMessageBox();
            } catch (IOException ioe){
                System.err.println(ioe.getLocalizedMessage());
                ioe.printStackTrace();
            }
        }

        public StartProcessThread() {
            super.start();
        }
    }

    //private UserInput userInput_;
    private CspProcess cspProcess_;
    private Properties properties_;
    private int solutionBy;
    private int gxFn=-1;
    private int maxDynamicTime = 0;
    private int runCountMatlab = 0;
    private Draw draw = null;

    private void chageEnabilityAllTextBoxes(boolean  enable){
        JTextField temp;
        for (Object j : this.mainPanel.getComponents()) {            
            if( j instanceof  JTextField){
                temp =(JTextField)j ;
                temp.setText(null);
                temp.setEnabled(enable);
            }
        }
        this.txtCurPref.setText("0");
        this.txtPrevPref.setText("-1");
    }

    private void chageEnabilityAllButtons(boolean  enable){
        JButton temp;
        for (Object j : this.mainPanel.getComponents()) {            
            if( j instanceof  JButton){
                temp =(JButton)j ;
                temp.setEnabled(enable);
            }
        }
    }
    
    public CSP_GUIView(SingleFrameApplication app) {
        super(app);
        String version;
        initComponents();
        this.rdoSatisfaction.setSelected(true);
        solutionBy = Chromosome.BY_SATISFACTIONS;
        //this.btnTest.setText("Fill with Test values");
        chageEnabilityAllTextBoxes(false);
        properties_= new Properties();
        try {
            properties_.load(new FileInputStream("src/csp_gui/resources/CSP_GUIApp.properties"));
            version = properties_.getProperty("Application.version");
        } catch (IOException e) {
            version = "xxx";
        }  

        this.lblVersion.setText("Ver: " + version);
        this.jlblGxF.setText("Select Gx Function");
        this.runCountMatlab = 0;
        this.draw = null;
        
        //this.getRootPane().setDefaultButton(btnData);

        // <editor-fold defaultstate="collapsed" desc="Default Code">

        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        //progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    //progressBar.setVisible(true);
                    //progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    //progressBar.setVisible(false);
                    //progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String)(evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer)(evt.getNewValue());
                    //progressBar.setVisible(true);
                    //progressBar.setIndeterminate(false);
                    //progressBar.setValue(value);
                }
            }
        });

        // </editor-fold>
    }
    

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = CSP_GUIApp.getApplication().getMainFrame();
            aboutBox = new CSP_GUIAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        CSP_GUIApp.getApplication().show(aboutBox);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        rdoDiscrete = new javax.swing.JRadioButton();
        rdoContinuous = new javax.swing.JRadioButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        txtGen = new javax.swing.JTextField();
        txtPop = new javax.swing.JTextField();
        txtConstraints = new javax.swing.JTextField();
        txtMaxValues = new javax.swing.JTextField();
        txtMinValues = new javax.swing.JTextField();
        txtDecisionVar = new javax.swing.JTextField();
        txtOjbective = new javax.swing.JTextField();
        btnTest = new javax.swing.JButton();
        chkMinVal = new javax.swing.JCheckBox();
        chkMaxVal = new javax.swing.JCheckBox();
        btnStart = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        lblVersion = new javax.swing.JLabel();
        btnData = new javax.swing.JButton();
        chkSaveChromes = new javax.swing.JCheckBox();
        PanelSolveBy = new javax.swing.JPanel();
        rdoSatisfaction = new javax.swing.JRadioButton();
        rdoViolation = new javax.swing.JRadioButton();
        rdoRo = new javax.swing.JRadioButton();
        rdoFitness = new javax.swing.JRadioButton();
        chkMatlabDraw = new javax.swing.JCheckBox();
        txtCurPref = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        txtPrevPref = new javax.swing.JTextField();
        lblPrefSuggestion = new javax.swing.JLabel();
        cboGxFuncs = new javax.swing.JComboBox();
        jlblGxF = new javax.swing.JLabel();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        fchDataFile = new javax.swing.JFileChooser();
        bgrpDataType = new javax.swing.ButtonGroup();
        bgrpDataType.add(this.rdoContinuous);
        bgrpDataType.add(this.rdoDiscrete);
        bgrpSolveBy = new javax.swing.ButtonGroup();
        bgrpSolveBy.add(this.rdoRo);
        bgrpSolveBy.add(this.rdoSatisfaction);
        bgrpSolveBy.add(this.rdoViolation);
        bgrpSolveBy.add(this.rdoFitness);

        mainPanel.setName("mainPanel"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(csp_gui.CSP_GUIApp.class).getContext().getResourceMap(CSP_GUIView.class);
        rdoDiscrete.setText(resourceMap.getString("rdoDiscrete.text")); // NOI18N
        rdoDiscrete.setName("rdoDiscrete"); // NOI18N
        rdoDiscrete.setNextFocusableComponent(rdoContinuous);
        rdoDiscrete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdoDiscreteActionPerformed(evt);
            }
        });

        rdoContinuous.setText(resourceMap.getString("rdoContinuous.text")); // NOI18N
        rdoContinuous.setName("rdoContinuous"); // NOI18N
        rdoContinuous.setNextFocusableComponent(btnTest);
        rdoContinuous.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdoContinuousActionPerformed(evt);
            }
        });

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        jLabel7.setText(resourceMap.getString("jLabel7.text")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N

        txtGen.setText(resourceMap.getString("txtGen.text")); // NOI18N
        txtGen.setName("txtGen"); // NOI18N
        txtGen.setNextFocusableComponent(chkSaveChromes);
        txtGen.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtGenFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtGenFocusLost(evt);
            }
        });

        txtPop.setText(resourceMap.getString("txtPop.text")); // NOI18N
        txtPop.setName("txtPop"); // NOI18N
        txtPop.setNextFocusableComponent(txtGen);
        txtPop.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtPopFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtPopFocusLost(evt);
            }
        });

        txtConstraints.setText(resourceMap.getString("txtConstraints.text")); // NOI18N
        txtConstraints.setName("txtConstraints"); // NOI18N
        txtConstraints.setNextFocusableComponent(chkMinVal);

        txtMaxValues.setText(resourceMap.getString("txtMaxValues.text")); // NOI18N
        txtMaxValues.setName("txtMaxValues"); // NOI18N
        txtMaxValues.setNextFocusableComponent(txtConstraints);

        txtMinValues.setText(resourceMap.getString("txtMinValues.text")); // NOI18N
        txtMinValues.setName("txtMinValues"); // NOI18N
        txtMinValues.setNextFocusableComponent(txtMaxValues);

        txtDecisionVar.setText(resourceMap.getString("txtDecisionVar.text")); // NOI18N
        txtDecisionVar.setName("txtDecisionVar"); // NOI18N
        txtDecisionVar.setNextFocusableComponent(txtMinValues);

        txtOjbective.setText(resourceMap.getString("txtOjbective.text")); // NOI18N
        txtOjbective.setName("txtOjbective"); // NOI18N
        txtOjbective.setNextFocusableComponent(txtDecisionVar);

        btnTest.setText(resourceMap.getString("btnTest.text")); // NOI18N
        btnTest.setName("btnTest"); // NOI18N
        btnTest.setNextFocusableComponent(btnData);
        btnTest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTestActionPerformed(evt);
            }
        });

        chkMinVal.setText(resourceMap.getString("chkMinVal.text")); // NOI18N
        chkMinVal.setName("chkMinVal"); // NOI18N
        chkMinVal.setNextFocusableComponent(chkMaxVal);
        chkMinVal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkMinValActionPerformed(evt);
            }
        });

        chkMaxVal.setText(resourceMap.getString("chkMaxVal.text")); // NOI18N
        chkMaxVal.setName("chkMaxVal"); // NOI18N
        chkMaxVal.setNextFocusableComponent(txtPop);
        chkMaxVal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkMaxValActionPerformed(evt);
            }
        });

        btnStart.setText(resourceMap.getString("btnStart.text")); // NOI18N
        btnStart.setName("btnStart"); // NOI18N
        btnStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStartActionPerformed(evt);
            }
        });

        jSeparator1.setName("jSeparator1"); // NOI18N

        lblVersion.setFont(resourceMap.getFont("lblVersion.font")); // NOI18N
        lblVersion.setText(resourceMap.getString("lblVersion.text")); // NOI18N
        lblVersion.setName("lblVersion"); // NOI18N

        btnData.setText(resourceMap.getString("btnData.text")); // NOI18N
        btnData.setEnabled(false);
        btnData.setName("btnData"); // NOI18N
        btnData.setNextFocusableComponent(txtOjbective);
        btnData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDataActionPerformed(evt);
            }
        });

        chkSaveChromes.setSelected(true);
        chkSaveChromes.setText(resourceMap.getString("chkSaveChromes.text")); // NOI18N
        chkSaveChromes.setName("chkSaveChromes"); // NOI18N
        chkSaveChromes.setNextFocusableComponent(rdoSatisfaction);

        PanelSolveBy.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(-16777216,true), 1, true));
        PanelSolveBy.setName("PanelSolveBy"); // NOI18N

        rdoSatisfaction.setSelected(true);
        rdoSatisfaction.setText(resourceMap.getString("rdoSatisfaction.text")); // NOI18N
        rdoSatisfaction.setName("rdoSatisfaction"); // NOI18N
        rdoSatisfaction.setNextFocusableComponent(rdoViolation);
        rdoSatisfaction.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdoSatisfactionActionPerformed(evt);
            }
        });

        rdoViolation.setText(resourceMap.getString("rdoViolation.text")); // NOI18N
        rdoViolation.setName("rdoViolation"); // NOI18N
        rdoViolation.setNextFocusableComponent(rdoRo);
        rdoViolation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdoViolationActionPerformed(evt);
            }
        });

        rdoRo.setText(resourceMap.getString("rdoRo.text")); // NOI18N
        rdoRo.setName("rdoRo"); // NOI18N
        rdoRo.setNextFocusableComponent(rdoFitness);
        rdoRo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdoRoActionPerformed(evt);
            }
        });

        rdoFitness.setText(resourceMap.getString("rdoFitness.text")); // NOI18N
        rdoFitness.setName("rdoFitness"); // NOI18N
        rdoFitness.setNextFocusableComponent(chkMatlabDraw);
        rdoFitness.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdoFitnessActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout PanelSolveByLayout = new javax.swing.GroupLayout(PanelSolveBy);
        PanelSolveBy.setLayout(PanelSolveByLayout);
        PanelSolveByLayout.setHorizontalGroup(
            PanelSolveByLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelSolveByLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(PanelSolveByLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(rdoViolation)
                    .addComponent(rdoSatisfaction)
                    .addComponent(rdoRo)
                    .addComponent(rdoFitness))
                .addContainerGap(30, Short.MAX_VALUE))
        );
        PanelSolveByLayout.setVerticalGroup(
            PanelSolveByLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelSolveByLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(rdoSatisfaction)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rdoViolation)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(rdoRo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(rdoFitness))
        );

        chkMatlabDraw.setText(resourceMap.getString("chkMatlabDraw.text")); // NOI18N
        chkMatlabDraw.setName("chkMatlabDraw"); // NOI18N
        chkMatlabDraw.setNextFocusableComponent(btnStart);

        txtCurPref.setText(resourceMap.getString("txtCurPref.text")); // NOI18N
        txtCurPref.setName("txtCurPref"); // NOI18N

        jLabel8.setText(resourceMap.getString("jLabel8.text")); // NOI18N
        jLabel8.setName("jLabel8"); // NOI18N

        jLabel9.setText(resourceMap.getString("jLabel9.text")); // NOI18N
        jLabel9.setName("jLabel9"); // NOI18N

        txtPrevPref.setText(resourceMap.getString("txtPrevPref.text")); // NOI18N
        txtPrevPref.setName("txtPrevPref"); // NOI18N

        lblPrefSuggestion.setText(resourceMap.getString("lblPrefSuggestion.text")); // NOI18N
        lblPrefSuggestion.setName("lblPrefSuggestion"); // NOI18N

        cboGxFuncs.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0", "240", "241", "243", "244", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12" }));
        cboGxFuncs.setName("cboGxFuncs"); // NOI18N
        cboGxFuncs.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cboGxFuncsItemStateChanged(evt);
            }
        });
        cboGxFuncs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboGxFuncsActionPerformed(evt);
            }
        });

        jlblGxF.setText(resourceMap.getString("jlblGxF.text")); // NOI18N
        jlblGxF.setName("jlblGxF"); // NOI18N

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, mainPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 657, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, mainPanelLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jLabel3)
                        .addGap(143, 143, 143)
                        .addComponent(txtMinValues, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(chkMinVal))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, mainPanelLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jLabel4)
                        .addGap(122, 122, 122)
                        .addComponent(txtMaxValues, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(chkMaxVal))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, mainPanelLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jLabel5)
                        .addGap(291, 291, 291)
                        .addComponent(txtConstraints, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGap(46, 46, 46)
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(mainPanelLayout.createSequentialGroup()
                                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel6)
                                    .addComponent(jLabel7))
                                .addGap(22, 22, 22)
                                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtPop, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtGen, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(chkSaveChromes)
                            .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(mainPanelLayout.createSequentialGroup()
                                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(txtCurPref, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtPrevPref, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 69, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblPrefSuggestion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(PanelSolveBy, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(chkMatlabDraw)
                            .addComponent(btnStart)))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, mainPanelLayout.createSequentialGroup()
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(mainPanelLayout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addComponent(jLabel2)
                                .addGap(234, 234, 234)
                                .addComponent(txtDecisionVar, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(mainPanelLayout.createSequentialGroup()
                                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addGroup(mainPanelLayout.createSequentialGroup()
                                        .addContainerGap()
                                        .addComponent(lblVersion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addGroup(mainPanelLayout.createSequentialGroup()
                                        .addGap(10, 10, 10)
                                        .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 118, Short.MAX_VALUE)))
                                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                                        .addGap(29, 29, 29)
                                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(rdoDiscrete)
                                            .addComponent(rdoContinuous))
                                        .addGap(146, 146, 146))
                                    .addGroup(mainPanelLayout.createSequentialGroup()
                                        .addGap(253, 253, 253)
                                        .addComponent(txtOjbective, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(27, 27, 27)))))
                        .addGap(27, 27, 27)
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jlblGxF, javax.swing.GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE)
                            .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(btnTest, javax.swing.GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE)
                                .addComponent(cboGxFuncs, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnData, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 13, Short.MAX_VALUE)))
                .addContainerGap(17, Short.MAX_VALUE))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(btnTest, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(rdoDiscrete, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(lblVersion))
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(rdoContinuous)
                        .addGap(7, 7, 7)
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(mainPanelLayout.createSequentialGroup()
                                .addGap(1, 1, 1)
                                .addComponent(txtOjbective, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGap(11, 11, 11)
                        .addComponent(btnData)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jlblGxF, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(7, 7, 7)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(jLabel2))
                    .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtDecisionVar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(cboGxFuncs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(10, 10, 10)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addComponent(jLabel3))
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(txtMinValues, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(chkMinVal))
                .addGap(3, 3, 3)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addComponent(jLabel4))
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(txtMaxValues, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(chkMaxVal))
                .addGap(4, 4, 4)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(jLabel5))
                    .addComponent(txtConstraints, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(22, 22, 22)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, mainPanelLayout.createSequentialGroup()
                            .addComponent(chkMatlabDraw)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnStart))
                        .addComponent(PanelSolveBy, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(txtPop, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7)
                            .addComponent(txtGen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(chkSaveChromes)))
                .addGap(19, 19, 19)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(txtPrevPref, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtCurPref, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblPrefSuggestion))
                    .addComponent(jLabel8))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(csp_gui.CSP_GUIApp.class).getContext().getActionMap(CSP_GUIView.class, this);
        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(statusPanelSeparator, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 664, Short.MAX_VALUE)
                    .addGroup(statusPanelLayout.createSequentialGroup()
                        .addComponent(statusMessageLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, 656, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(statusAnimationLabel)))
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, statusPanelLayout.createSequentialGroup()
                .addContainerGap(13, Short.MAX_VALUE)
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, statusPanelLayout.createSequentialGroup()
                        .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(statusMessageLabel)
                            .addComponent(statusAnimationLabel))
                        .addGap(3, 3, 3))
                    .addComponent(progressBar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        fchDataFile.setName("fchDataFile"); // NOI18N

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

    private void rdoDiscreteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdoDiscreteActionPerformed
        chageEnabilityAllTextBoxes(true); // TODO add your handling code here:
}//GEN-LAST:event_rdoDiscreteActionPerformed

    private void rdoContinuousActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdoContinuousActionPerformed
        chageEnabilityAllTextBoxes(true);
}//GEN-LAST:event_rdoContinuousActionPerformed

    private void btnTestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTestActionPerformed
        // TODO add your handling code here:
      
        gxFn = Integer.parseInt(cboGxFuncs.getSelectedItem().toString());
               
        switch (gxFn) {
            case 240:
                //G24_0
                //total vars (n) = 2
                //total constraints = 2+2
                //0 <= x0 <= 3, 
                //0<=x1<=4, 
                this.txtConstraints.setText("4");
                this.txtDecisionVar.setText("2");
                this.txtMaxValues.setText("3,4");
                this.txtMinValues.setText("0,0");
                this.txtOjbective.setText("1");
                this.txtGen.setText("400");
                this.txtPop.setText("100"); 
                this.maxDynamicTime = 10; 
            break;
                
            case 241:
                //G24_1
                //total vars (n) = 2
                //total constraints = 2+2
                //0 <= x0 <= 3, 
                //0<=x1<=4, 
                this.txtConstraints.setText("4");
                this.txtDecisionVar.setText("2");
                this.txtMaxValues.setText("3,4");
                this.txtMinValues.setText("0,0");
                this.txtOjbective.setText("1");
                this.txtGen.setText("400");
                this.txtPop.setText("100"); 
                this.maxDynamicTime = 10; 
            break;
                
            case 243:
                //G24_3
                //total vars (n) = 2
                //total constraints = 2+2
                //0 <= x0 <= 3, 
                //0<=x1<=4, 
                this.txtConstraints.setText("4");
                this.txtDecisionVar.setText("2");
                this.txtMaxValues.setText("3,4");
                this.txtMinValues.setText("0,0");
                this.txtOjbective.setText("1");
                this.txtGen.setText("400");
                this.txtPop.setText("100"); 
                this.maxDynamicTime = 10; 
            break;
                
            case 244:
                //G24_4
                //total vars (n) = 2
                //total constraints = 2+2
                //0 <= x0 <= 3, 
                //0<=x1<=4, 
                this.txtConstraints.setText("4");
                this.txtDecisionVar.setText("2");
                this.txtMaxValues.setText("3,4");
                this.txtMinValues.setText("0,0");
                this.txtOjbective.setText("1");
                this.txtGen.setText("400");
                this.txtPop.setText("100");
                this.maxDynamicTime = 10; 
            break;
            
            case 1:                            
                //G1
                // total vars = 13
                // total constraints = 9+13=22
                //0 <= xi <= 1, i = 0-8, 
                //0 <=xi <= 100, i = 9,10,11
                //0 <= x12 <= 1.
                this.txtConstraints.setText("22");
                this.txtDecisionVar.setText("13");
                this.txtMaxValues.setText("1,1,1,1,1,1,1,1,1,100,100,100,1");
                this.txtMinValues.setText("0,0,0,0,0,0,0,0,0,0,0,0,0");
                this.txtOjbective.setText("1");
                this.txtGen.setText("2000");
                this.txtPop.setText("100");
            break;
    
            case 2:        
                //G2
                // total vars (n) = 20 (prefered)
                // total constraints = 20+2
                //0 <= xi <= 10, i = 0-n, 
                this.txtConstraints.setText("22");
                this.txtDecisionVar.setText("20");
                this.txtMaxValues.setText("10");
                this.txtMinValues.setText("0");
                this.txtOjbective.setText("1");
                this.txtGen.setText("2000");
                this.txtPop.setText("100");
                this.chkMinVal.setSelected(true);
                this.chkMaxVal.setSelected(true);
                this.chkMinValActionPerformed(evt);
                this.chkMaxValActionPerformed(evt);
            break;
    
            case 3:
                //G3
                // total vars (n) = 10 (prefered)
                // total constraints = 1+10
                //0 <= xi <=1, i = 0-n, 
                this.txtConstraints.setText("11");
                this.txtDecisionVar.setText("10");
                this.txtMaxValues.setText("1");
                this.txtMinValues.setText("0");
                this.txtOjbective.setText("1");
                this.txtGen.setText("2000");
                this.txtPop.setText("100");
                this.chkMinVal.setSelected(true);
                this.chkMaxVal.setSelected(true);
                this.chkMinValActionPerformed(evt);
                this.chkMaxValActionPerformed(evt);
            break;
    
            case 4:
                //G4
                // total vars (n) = 5
                // total constraints = 3*2+5 = 6+5 = 11
                //78 <= x0 <= 102
                //33 <= x1 <= 45
                //27 <= xi <= 45 ,i = 2, 3, 4    
                this.txtConstraints.setText("11");
                this.txtDecisionVar.setText("5");
                this.txtMaxValues.setText("102,45,45,45,45");
                this.txtMinValues.setText("78,33,27,27,27");
                this.txtOjbective.setText("1");
                this.txtGen.setText("2000");
                this.txtPop.setText("100");
            break;
    
            case 5:
                //G5         
                // total vars (n) = 4
                // total constraints = 5+4
                // 0 <= xi <= 1200, i = 0, 1
                // -0.55<=xi<=0.55, i = 2, 3
                this.txtConstraints.setText("9");
                this.txtDecisionVar.setText("4");
                this.txtMaxValues.setText("1200,1200,0.55,0.55");
                this.txtMinValues.setText("0,0,-0.55,-0.55");
                this.txtOjbective.setText("1");
                this.txtGen.setText("100000");
                this.txtPop.setText("25");
            break;
                
            case 6:
                //G6
                // total vars (n) = 2 
                // total constraints = 2+2
                // 13 <= x0 <= 100,
                // 0 <= x1 <= 100,
                this.txtConstraints.setText("4");
                this.txtDecisionVar.setText("2");
                this.txtMaxValues.setText("100,100");
                this.txtMinValues.setText("13,0");
                this.txtOjbective.setText("1");
                this.txtGen.setText("2000");
                this.txtPop.setText("100");
            break;
    
            case 7:
                //G7
                // total vars (n) = 10
                // total constraints = 8+10
                // -10.0 <=xi <= 10.0, i = 0..9
                this.txtConstraints.setText("18");
                this.txtDecisionVar.setText("10");
                this.txtMaxValues.setText("10");
                this.txtMinValues.setText("-10");
                this.txtOjbective.setText("1");
                this.txtGen.setText("2000");
                this.txtPop.setText("100");
                this.chkMinVal.setSelected(true);
                this.chkMaxVal.setSelected(true);
                this.chkMinValActionPerformed(evt);
                this.chkMaxValActionPerformed(evt);
            break;
    
            case 8:
                //G8
                // total vars (n) = 2 
                // total constraints = 2+2
                // 0 <= x0 <= 10,
                // 0 <= x1 <= 10,
                this.txtConstraints.setText("4");
                this.txtDecisionVar.setText("2");
                this.txtMaxValues.setText("10, 10");
                this.txtMinValues.setText("0, 0");
                this.txtOjbective.setText("1");
                this.txtGen.setText("2000");
                this.txtPop.setText("100");
            break;
    
            case 9:
                //G9
                // total vars (n) = 7
                // total constraints = 4 + 7
                // -10<=xi<=10, i = 0..6
                this.txtConstraints.setText("11");
                this.txtDecisionVar.setText("7");
                this.txtMaxValues.setText("10,10,10,10,10,10,10");
                this.txtMinValues.setText("-10,-10,-10,-10,-10,-10,-10");
                this.txtOjbective.setText("1");
                this.txtGen.setText("2000");
                this.txtPop.setText("100");
            break;
    
            case 10:   
                //G10
                // total vars (n) = 8
                // total constraints = 6+8
                // 100<=xi<=10,000 i = 1
                // 1000<=xi<=10,000 i = 2,3
                // 10<=xi<=1000 i = 4..8
                this.txtConstraints.setText("14");
                this.txtDecisionVar.setText("8");
                this.txtMaxValues.setText("10000, 10000, 10000, 1000, 1000, 1000, 1000, 1000");
                this.txtMinValues.setText("100, 1000, 1000, 10, 10, 10, 10, 10");
                this.txtOjbective.setText("1");
                this.txtGen.setText("2000");
                this.txtPop.setText("100");
            break;
    
            case 11:
                //G11
                // total vars (n) = 2
                // total constraints = 1+2
                // -1<=xi<=1 i = 0,1
                this.txtConstraints.setText("3");
                this.txtDecisionVar.setText("2");
                this.txtMaxValues.setText("1, 1");
                this.txtMinValues.setText("-1, -1");
                this.txtOjbective.setText("1");
                this.txtGen.setText("2000");
                this.txtPop.setText("100");
            break;
                
            case 12:
                //G12
                // total vars (n) = 3
                // total constraints = 3+729 
                // 0<=xi<=10 ii = 0,1,2
                this.txtConstraints.setText("732");
                this.txtDecisionVar.setText("3");
                this.txtMaxValues.setText("10,10,10");
                this.txtMinValues.setText("0,0,0");
                this.txtOjbective.setText("1");
                this.txtGen.setText("2000");
                this.txtPop.setText("100");
            break;
                
            default: 
                throw new UnsupportedOperationException("invalid choice of gx function");
//            break;
        }
        
        
        //alkyl
//        this.txtConstraints.setText("21");
//        this.txtDecisionVar.setText("14");
//        this.txtMaxValues.setText("2,1.6,1.2,5,2,0.93,0.95,12,4,1.62,1.01010101010101,1.01010101010101,1.11111111111111,1.01010101010101");
//        this.txtMinValues.setText("0,0,0,0,0,0.85,0.9,3.0,1.2,1.45,0.99,0.99,0.9,0.99");
//        this.txtOjbective.setText("1");
//        this.txtGen.setText("100000");
//        this.txtPop.setText("25");

        //hs109
        // total vars (n) = 9
        // total constraints = 10+9
        // 0<= x[ii] <= inf; ii = 0,1 
        // -.55 <= x[ii] <= .55; ii = 2,3
	// 196 <= x[ii] <= 252; ii = 4, 5, 6,
        // -400 <= x[ii] <= 800; ii = 7, 8
//        this.txtConstraints.setText("19");
//        this.txtDecisionVar.setText("9");
//        this.txtMaxValues.setText("1.0e8,1.0e8, 0.55, 0.55, 252,252,252, 800, 800");
//        this.txtMinValues.setText("0,0, -0.55,-0.55, 196,196,196, -400,-400");
//        this.txtOjbective.setText("1");
//        this.txtGen.setText("1000");
//        this.txtPop.setText("100");
        
        // broyden
//        this.txtConstraints.setText("20");
//        this.txtDecisionVar.setText("10");
//        this.txtMaxValues.setText("1.0E8");
//        this.txtMinValues.setText("-1.0E8");
//        this.txtOjbective.setText("1");
//        this.txtGen.setText("10000");
//        this.txtPop.setText("100");
        
        
        //G24_4
        // total vars (n) = 2
        // total constraints = 2+2
        // 0 <= x0 <= 3, 
        // 0<=x1<=4, 
//        this.txtConstraints.setText("4");
//        this.txtDecisionVar.setText("2");
//        this.txtMaxValues.setText("3,4");
//        this.txtMinValues.setText("0,0");
//        this.txtOjbective.setText("1");
//        this.txtGen.setText("999");
//        this.txtPop.setText("100");
        
        //h77
        // total vars (n) = 5
        // total constraints = 3+5
        // 0 <= xi <= 4, ii = 0..4
//        this.txtConstraints.setText("8");
//        this.txtDecisionVar.setText("5");
//        this.txtMaxValues.setText("4,4,4,4,4");
//        this.txtMinValues.setText("0,0,0,0,0");
//        this.txtOjbective.setText("1");
//        this.txtGen.setText("1001");
//        this.txtPop.setText("100");
        
//        this.chkMinVal.setSelected(true);
//        this.chkMaxVal.setSelected(true);
//        this.chkMinValActionPerformed(evt);
//        this.chkMaxValActionPerformed(evt);
}//GEN-LAST:event_btnTestActionPerformed

    private void chkMinValActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkMinValActionPerformed
        // TODO add your handling code here:
        StringTokenizer min_str = new StringTokenizer(this.txtMinValues.getText().replaceAll(" ", ""),",");

        String str;
        String sameVal;

        if (this.chkMinVal.isSelected()){
            try {
                sameVal = min_str.nextElement().toString();
                str = sameVal;
                for (int i = 0; i < Integer.parseInt(this.txtDecisionVar.getText())-1; i++) {
                    str = str+","+sameVal;
                }
                this.txtMinValues.setText(str);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
}//GEN-LAST:event_chkMinValActionPerformed

    private void chkMaxValActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkMaxValActionPerformed
        // TODO add your handling code here:
        StringTokenizer min_str = new StringTokenizer(this.txtMaxValues.getText().replaceAll(" ", ""),",");

        String str;
        String sameVal;

        if (this.chkMaxVal.isSelected()){
            try {
                sameVal = min_str.nextElement().toString();
                str = sameVal;
                for (int i = 0; i < Integer.parseInt(this.txtDecisionVar.getText())-1; i++) {
                    str = str+","+sameVal;
                }
                this.txtMaxValues.setText(str);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
}//GEN-LAST:event_chkMaxValActionPerformed

        private Object castString(Class t, String data){
        Object obj;

        if (t.getName().contains("Double") || t.getName().contains("double")){
            obj = Double.parseDouble(data);
        }
        else if(t.getName().contains("Integer") || t.getName().contains("int")){
            obj = Integer.parseInt(data);
        }
        else{
            obj = null;
        }
        return obj;
    }
        
    private void btnStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStartActionPerformed

        UserInput userInput;
        Class c;

        try {
            if (this.rdoContinuous.isSelected()) {
                c = Double.class;
            } else if(this.rdoDiscrete.isSelected()){
                c = Integer.class;
            } else {
                throw new MyException("Select an option for type of data", "Incomplete Form", JOptionPane.WARNING_MESSAGE);
            }

            userInput = new UserInput(c, chkSaveChromes.isSelected());            
            userInput.totalConstraints = Integer.parseInt(this.txtConstraints.getText());
            userInput.totalDecisionVars = Integer.parseInt(this.txtDecisionVar.getText());
            userInput.totalObjectives = Integer.parseInt(this.txtOjbective.getText());
            userInput.solutionBy = this.solutionBy;
            userInput.gxFn = this.gxFn;
            userInput.maxDynamicTime = this.maxDynamicTime;
            if(userInput.maxDynamicTime == 0){
                userInput.maxDynamicTime = userInput.totalConstraints-userInput.totalDecisionVars;
            }
            
            StringTokenizer min_str = new StringTokenizer(this.txtMinValues.getText().replaceAll(" ", ""),",");
            StringTokenizer max_str = new StringTokenizer(this.txtMaxValues.getText().replaceAll(" ", ""),",");

            if(min_str.countTokens() != max_str.countTokens()){
                throw new Exception();
            }
            int sz; // it is a MUST
            sz = min_str.countTokens();

            for (int i = 0; i < sz; i++) {
                userInput.minVals.add(Double.valueOf(min_str.nextElement().toString()));
                userInput.maxVals.add(Double.valueOf(max_str.nextElement().toString()));
            }

            userInput.population = Integer.parseInt(this.txtPop.getText());
            userInput.generation = Integer.parseInt(this.txtGen.getText());

            //            int sz;
            //            String str = "";
            //            Object obj;
            //            sz = max_str.countTokens();
            //            for (int i = 0; i < sz; i++) {
            //                str = max_str.nextElement().toString();
            //                obj = castString(c, str);
            //                userInput_.maxVals.add(c.cast(obj));
            //            }

            userInput.validateData();
            System.out.println(userInput);

            cspProcess_ = new CspProcess(userInput);
            cspProcess_.bMatlabDraw = this.chkMatlabDraw.isSelected();
            
            if(cspProcess_.bMatlabDraw && this.runCountMatlab == 0){
                draw = new Draw();
            }

            new StartProcessThread();
            

        } catch (MyException e){
            e.showMessageBox();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Check your data!", "Incorrect Data", JOptionPane.ERROR_MESSAGE);
        } 
        
        if(this.chkMatlabDraw.isSelected()){ //count how many time running with matlab startDrawing enabled.
            this.runCountMatlab++;
        }
    }//GEN-LAST:event_btnStartActionPerformed

    private void btnDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDataActionPerformed
        class MyCustomFilter extends javax.swing.filechooser.FileFilter {
            @Override
            public boolean accept(File file) {
                // Allow only directories, or files with ".txt" extension
                return file.isDirectory() || file.getAbsolutePath().endsWith(".txt") 
                        || file.getAbsolutePath().endsWith(".dat")
                        || file.getAbsolutePath().endsWith(".col");
            }
            
            @Override
            public String getDescription() {
                // This description will be displayed in the dialog,
                // hard-coded = ugly, should be done via I18N
                return "Text documents (*.txt; *.dat; *.col)";
            }
        } 

        File f;
        try{
            f = new File(new File(".").getCanonicalPath());
            this.fchDataFile = new javax.swing.JFileChooser();
            this.fchDataFile.setFileFilter(new MyCustomFilter()); 
            this.fchDataFile.setCurrentDirectory(f);
        } catch (IOException e) {
            e.printStackTrace();
            Application.getInstance().exit();
        }
        
        
        
        int returnVal = this.fchDataFile.showOpenDialog(null);
        ExternalData edata;
        File file = null;
        //Scanner readFile = null;
        String errmsg = "File not found";
        Class c;
        
        try {
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                file = fchDataFile.getSelectedFile();
                //System.out.println(file.getAbsolutePath());
                //readFile = new Scanner(file);
            }else{
                //readFile = null;
                errmsg = "No File Selected";
                throw new FileNotFoundException(errmsg);
            }

        } catch (FileNotFoundException fnfe) {
            System.err.println(fnfe.getLocalizedMessage());
            MyException e = new MyException(errmsg, "Incorrect File",JOptionPane.INFORMATION_MESSAGE);
            e.showMessageBox();
        }
 
        try{
            if (this.rdoContinuous.isSelected()) {
                c = Double.class;
            } else if(this.rdoDiscrete.isSelected()){
                c = Integer.class;
            } else {
                throw new MyException("Select an option for type of data", "Incomplete Form", JOptionPane.WARNING_MESSAGE);
            }
            
//            edata = new TimeTableData(file.getAbsolutePath(), Integer.parseInt(this.txtPop.getText()), 
//                    Integer.parseInt(this.txtGen.getText()), Integer.parseInt(txtCurPref.getText()), 
//                    Integer.parseInt(txtPrevPref.getText()), chkSaveChromes.isSelected(), this.solutionBy, c);
            edata = null;

            System.out.println(edata.getUserInput());

            this.chageEnabilityAllButtons(false);            
            cspProcess_ = new CspProcess(edata);
            cspProcess_.bMatlabDraw = this.chkMatlabDraw.isSelected();
            new StartProcessThread();

            this.chageEnabilityAllButtons(true);
            System.out.println();

            
            
        }catch(MyException me){
            me.showMessageBox();
        } catch (UnsupportedOperationException uoe){
            System.err.println(uoe.getLocalizedMessage());
        } catch (Exception e) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ie) {
            }

            JOptionPane.showMessageDialog(null, "Check your data!", "Incorrect Data", JOptionPane.ERROR_MESSAGE);
            System.err.println(e.getLocalizedMessage());
            e.printStackTrace();
            Application.getInstance().exit();
            //e.printStackTrace();
        }
        // edata.readData();
    }//GEN-LAST:event_btnDataActionPerformed

    private void txtPopFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtPopFocusLost
        // TODO add your handling code here:
        try{
            if(Integer.valueOf(this.txtPop.getText()) == null){
                JOptionPane.showMessageDialog(null, "Enter Population", "Incorrect Data", JOptionPane.WARNING_MESSAGE);
            }else{
                this.btnData.setEnabled(true);
            }
        } catch (NumberFormatException e){
            JOptionPane.showMessageDialog(null, "Enter integer value for population", "Incorrect Data", JOptionPane.WARNING_MESSAGE);
            //this.txtPop.requestFocusInWindow();
            
        }
    }//GEN-LAST:event_txtPopFocusLost

    private void txtPopFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtPopFocusGained
        // TODO add your handling code here:
        this.txtPop.selectAll();
    }//GEN-LAST:event_txtPopFocusGained

    private void txtGenFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtGenFocusLost
        // TODO add your handling code here:
        this.btnData.setEnabled(true);
        if(cspProcess_ != null){
            cspProcess_.userInput_.generation = Integer.parseInt(this.txtGen.getText());
        }
    }//GEN-LAST:event_txtGenFocusLost

    private void rdoSatisfactionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdoSatisfactionActionPerformed
        // TODO add your handling code here:
        this.solutionBy = Chromosome.BY_SATISFACTIONS;
    }//GEN-LAST:event_rdoSatisfactionActionPerformed

    private void rdoViolationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdoViolationActionPerformed
        // TODO add your handling code here:
        this.solutionBy = Chromosome.BY_VIOLATIONS;
    }//GEN-LAST:event_rdoViolationActionPerformed

    private void rdoRoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdoRoActionPerformed
        // TODO add your handling code here:
        this.solutionBy = Chromosome.BY_RO;
    }//GEN-LAST:event_rdoRoActionPerformed

    private void rdoFitnessActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdoFitnessActionPerformed
        // TODO add your handling code here:
        this.solutionBy = Chromosome.BY_FITNESS;
    }//GEN-LAST:event_rdoFitnessActionPerformed

    private void txtGenFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtGenFocusGained
        // TODO add your handling code here:
        this.txtGen.selectAll();
    }//GEN-LAST:event_txtGenFocusGained

    private void cboGxFuncsItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cboGxFuncsItemStateChanged
        // TODO add your handling code here:
        
    }//GEN-LAST:event_cboGxFuncsItemStateChanged

    private void cboGxFuncsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboGxFuncsActionPerformed
        // TODO add your handling code here:
        btnTestActionPerformed(evt);        
    }//GEN-LAST:event_cboGxFuncsActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel PanelSolveBy;
    private javax.swing.ButtonGroup bgrpDataType;
    private javax.swing.ButtonGroup bgrpSolveBy;
    private javax.swing.JButton btnData;
    private javax.swing.JButton btnStart;
    private javax.swing.JButton btnTest;
    private javax.swing.JComboBox cboGxFuncs;
    private javax.swing.JCheckBox chkMatlabDraw;
    private javax.swing.JCheckBox chkMaxVal;
    private javax.swing.JCheckBox chkMinVal;
    private javax.swing.JCheckBox chkSaveChromes;
    private javax.swing.JFileChooser fchDataFile;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel jlblGxF;
    private javax.swing.JLabel lblPrefSuggestion;
    private javax.swing.JLabel lblVersion;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JRadioButton rdoContinuous;
    private javax.swing.JRadioButton rdoDiscrete;
    private javax.swing.JRadioButton rdoFitness;
    private javax.swing.JRadioButton rdoRo;
    private javax.swing.JRadioButton rdoSatisfaction;
    private javax.swing.JRadioButton rdoViolation;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JTextField txtConstraints;
    private javax.swing.JTextField txtCurPref;
    private javax.swing.JTextField txtDecisionVar;
    private javax.swing.JTextField txtGen;
    private javax.swing.JTextField txtMaxValues;
    private javax.swing.JTextField txtMinValues;
    private javax.swing.JTextField txtOjbective;
    private javax.swing.JTextField txtPop;
    private javax.swing.JTextField txtPrevPref;
    // End of variables declaration//GEN-END:variables

    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;

    private JDialog aboutBox;
}
