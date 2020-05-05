import builders.MediaBuilder;
import builders.StdMediaBuilder;
import exceptions.MediaTimerAlreadyDefinedException;
import exceptions.UndefinedMediaTimerException;
import file.MediaLoader;
import media.ListMedia;
import media.Media;
import player.PlayerModel;
import player.StdPlayerModel;
import timer.MediaTimer;
import timer.StdMediaTimer;
import timer.TimerState;
import view.JButtonFactory;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;

public class GraphicPlayer {

    private PlayerModel model;
    private MediaTimer timer;

    private JFrame frame;

    private JLabel currentMedia;
    private JProgressBar progressBar;

    private JButton playBtn;
    private JButton pauseBtn;
    private JButton resumeBtn;
    private JButton nextMediaBtn;
    private JButton previousMediaBtn;
    private JButton nextSublistBtn;
    private JButton previousSublistBtn;

    public GraphicPlayer(PlayerModel model){
        try {
            StdMediaTimer.setInstance();
            timer = StdMediaTimer.getInstance();
        } catch (MediaTimerAlreadyDefinedException | UndefinedMediaTimerException e) {
            e.printStackTrace();
        }

        this.model = model;

        createView();
        placeComponents();
        createControllers();
    }

    public void display(){
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void createView() {
        frame = new JFrame("Djubaka");
        frame.setPreferredSize(new Dimension(600,200));

        currentMedia = new JLabel("Aucun");
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setString("0 / 0 sec");
        progressBar.setPreferredSize(new Dimension(400, 30));

        playBtn = JButtonFactory.createStdButton("./resources/img/playIcon.png", "Play");
        pauseBtn = JButtonFactory.createStdButton("./resources/img/pauseIcon.png", "Pause");
        pauseBtn.setEnabled(false);
        resumeBtn = JButtonFactory.createStdButton("./resources/img/resumeIcon.png", "Resume");
        resumeBtn.setEnabled(false);
        nextMediaBtn = JButtonFactory.createStdButton("./resources/img/nextMediaIcon.png", "Next Media");
        nextMediaBtn.setEnabled(false);
        previousMediaBtn = JButtonFactory.createStdButton("./resources/img/previousMediaIcon.png", "Previous Media");
        previousMediaBtn.setEnabled(false);
        nextSublistBtn = JButtonFactory.createStdButton("./resources/img/nextParentMediaIcon.png", "Next Sublist");
        nextSublistBtn.setEnabled(false);
        previousSublistBtn = JButtonFactory.createStdButton("./resources/img/previousParentMediaIcon.png", "Previous Sublist");
        previousSublistBtn.setEnabled(false);
    }

    private void placeComponents() {
        JPanel p = new JPanel(); {
            p.setPreferredSize(new Dimension(150, (int) frame.getPreferredSize().getHeight()));
            p.setBorder(BorderFactory.createEtchedBorder());

            p.add(playBtn);

            JPanel q = new JPanel(); {
                q.add(pauseBtn);
                q.add(resumeBtn);
            }

            p.add(q);

            q = new JPanel(); {
                q.add(nextMediaBtn);
                q.add(previousMediaBtn);
            }

            p.add(q);

            q = new JPanel(); {
                q.add(nextSublistBtn);
                q.add(previousSublistBtn);
            }

            p.add(q);
        }

        frame.add(p, BorderLayout.WEST);

        p = new JPanel(new BorderLayout()); {
            p.setBorder(BorderFactory.createEtchedBorder());

            JPanel q = new JPanel(new BorderLayout()); {
                JPanel r = new JPanel(new GridLayout(3,1)); {
                    r.add(new JLabel("En cours de lecture : "));
                    r.add(new JLabel());
                    r.add(currentMedia);
                }

                q.add(r, BorderLayout.WEST);
            }

            p.add(q, BorderLayout.NORTH);

            q = new JPanel(new BorderLayout()); {
                q.add(progressBar, BorderLayout.SOUTH);
            }

            p.add(q);
        }

        frame.add(p);
    }

    private void createControllers() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        ChangeListener modelListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                try {
                    if (!model.isFinished()){
                        if (timer.getState() != TimerState.NOT_STARTED) timer.stop();
                        Media media = MediaLoader.loadCompleteMediaFromMediaFile(model.getCurrentMedia().getName());
                        model.setCurrentMediaInfo(media);

                        currentMedia.setText(media.toString());
                        progressBar.setMaximum(media.getDuration());

                        if (timer.getState() == TimerState.NOT_STARTED) timer.start();
                    } else {
                        timer.stop();
                        currentMedia.setText("Aucun");
                        progressBar.setValue(0);
                        progressBar.setString("0 / 0 sec");
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        };

        model.addChangeListener(modelListener);

        ChangeListener timerListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                try {
                    progressBar.setValue(timer.getTime());
                    progressBar.setString(timer.getTime() + " / " + model.getCurrentMediaInfo().getDuration() + " sec");

                    if (timer.getTime() == model.getCurrentMediaInfo().getDuration()) {
                        model.goToNextMedia(model.getCurrentMedia());
                    }
                } catch (Exception e){
                    System.out.println(e.getMessage());
                }
            }
        };

        timer.addChangeListener(timerListener);

        playBtn.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    model.startPlaylist();

                    playBtn.setEnabled(false);
                    pauseBtn.setEnabled(true);
                    nextMediaBtn.setEnabled(true);
                    previousMediaBtn.setEnabled(true);
                    nextSublistBtn.setEnabled(true);
                    previousSublistBtn.setEnabled(true);
                } catch (Exception e){
                    System.out.println(e.getMessage());
                }
            }
        });

        pauseBtn.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    timer.pause();

                    pauseBtn.setEnabled(false);
                    resumeBtn.setEnabled(true);
                } catch (Exception e){
                    System.out.println(e.getMessage());
                }
            }
        });

        resumeBtn.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    timer.resume();

                    pauseBtn.setEnabled(true);
                    resumeBtn.setEnabled(false);
                } catch (Exception e){
                    System.out.println(e.getMessage());
                }
            }
        });

        nextMediaBtn.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    model.goToNextMedia(model.getCurrentMedia());

                    if (!pauseBtn.isEnabled()) pauseBtn.setEnabled(true);
                    if (resumeBtn.isEnabled()) resumeBtn.setEnabled(false);
                } catch (Exception e){
                    System.out.println(e.getMessage());
                }

            }
        });

        previousMediaBtn.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    model.goToPreviousMedia(model.getCurrentMedia());

                    if (!pauseBtn.isEnabled()) pauseBtn.setEnabled(true);
                    if (resumeBtn.isEnabled()) resumeBtn.setEnabled(false);
                } catch (Exception e){
                    System.out.println(e.getMessage());
                }
            }
        });

        nextSublistBtn.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    model.goToNextParentMedia();

                    if (!pauseBtn.isEnabled()) pauseBtn.setEnabled(true);
                    if (resumeBtn.isEnabled()) resumeBtn.setEnabled(false);
                } catch (Exception e){
                    System.out.println(e.getMessage());
                }
            }
        });

        previousSublistBtn.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    model.goToPreviousParentMedia();

                    if (!pauseBtn.isEnabled()) pauseBtn.setEnabled(true);
                    if (resumeBtn.isEnabled()) resumeBtn.setEnabled(false);
                } catch (Exception e){
                    System.out.println(e.getMessage());
                }
            }
        });
    }

    public static void main(String[] args){
        String filename = "";
        String option = "";

        if (args.length < 1) {
            System.out.println("Error : you have to give a xpl list file");
            return;
        } else if (args.length == 1){
            filename = args[0];
        } else if (args.length == 2){
            if (args[0].equals("-d")) {
                option = args[0];
                filename = args[1];
            }
            else if (args[1].equals("-d")) {
                option = args[1];
                filename = args[0];
            }

            if (option.equals("")) {
                System.out.println("Error : use -d for debug mod");
                return;
            }
        }

        try {
            MediaBuilder builder = new StdMediaBuilder();
            ListMedia list = MediaLoader.loadListFromXPL(filename, builder);
            PlayerModel model = new StdPlayerModel(list);

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    new GraphicPlayer(model).display();
                }
            });

            if (option.equals("-d")) {
                Thread.sleep(500);
                new ConsolePlayer(model, true);
            }
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}
