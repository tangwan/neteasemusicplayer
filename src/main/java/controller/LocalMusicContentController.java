package controller;

import com.sun.javafx.scene.control.skin.TableHeaderRow;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import mediaplayer.MyMediaPlayer;
import mediaplayer.PlayMode;
import model.Song;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import application.SpringFXMLLoader;
import util.SongUtils;
import util.StageUtils;
import util.WindowUtils;
import util.XMLUtils;
import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Controller
public class LocalMusicContentController {

    /**本地音乐中间面板的根容器*/
    @FXML
    private BorderPane localMusicContentContainer;

    /**根容器的中间内容容器*/
    @FXML
    public BorderPane borderPane;

    @FXML
    private TableColumn<Song,String> nameColumn;

    @FXML
    private TableColumn<Song,String> singerColumn;

    @FXML
    private TableColumn<Song,String> albumColumn;

    @FXML
    private TableColumn<Song,String> totalTimeColumn;

    @FXML
    private TableColumn<Song,String> sizeColumn;

    /**“选择目录”的HBox容器*/
    @FXML
    private HBox hBoxChoseFolder;

    /**"歌曲"的HBox容器*/
    @FXML
    private HBox hBoxSong;

    /**"歌手"的HBox容器*/
    @FXML
    private HBox hBoxSinger;

    /**"专辑"的HBox容器*/
    @FXML
    private HBox hBoxAlbum;

    /**"歌曲"的计数Label*/
    @FXML
    private Label labSongCount;

    /**"歌曲"的计数Label容器*/
    @FXML
    private Label labSingerCount;

    /**"歌曲"的计数Label容器*/
    @FXML
    private Label labAlbumCount;

    /**装标签的集合tabList*/
    private List<HBox> tabList;

    /**显示进度的指示器*/
    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    private TableView<Song> tableViewSong;

    /**注入窗体根容器（BorderPane）的控制类*/
    @Resource
    MainController mainController;

    /**注入Spring上下文类*/
    @Resource
    private ApplicationContext applicationContext;

    /**注入“选择目录”舞台的controller*/
    @Resource
    private ChoseFolderController choseFolderController;

    /**注入播放器*/
    @Resource
    private MyMediaPlayer myMediaPlayer;

    /**注入窗体底部进度条的控制器*/
    @Resource
    private BottomController bottomController;

    public TableView<Song> getTableViewSong() {
        return tableViewSong;
    }

    public ProgressIndicator getProgressIndicator() {
        return progressIndicator;
    }

    public Label getLabSongCount() {
        return labSongCount;
    }

    public void initialize() throws Exception {
        tabList = new ArrayList<>();
        tabList.add(hBoxSong);
        tabList.add(hBoxSinger);
        tabList.add(hBoxAlbum);

        progressIndicator.setVisible(false);   //初始化进度指示器为不可见
        this.setSelectedTab(hBoxSong);  //设置初始选中为格式标签
        //添加css名称.在CSS文件定制样式
        nameColumn.getStyleClass().add("nameColumn");
        singerColumn.getStyleClass().add("singerColumn");
        albumColumn.getStyleClass().add("albumColumn");
        totalTimeColumn.getStyleClass().add("totalTimeColumn");
        sizeColumn.getStyleClass().add("sizeColumn");
        //列属性绑定
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        singerColumn.setCellValueFactory(new PropertyValueFactory<>("singer"));
        albumColumn.setCellValueFactory(new PropertyValueFactory<>("album"));
        totalTimeColumn.setCellValueFactory(new PropertyValueFactory<>("totalTime"));
        sizeColumn.setCellValueFactory(new PropertyValueFactory<>("size"));

        //关闭表格"头"列的左右拖拽移动重新排列行为
        tableViewSong.widthProperty().addListener(new ChangeListener<Number>()
        {
            @Override
            public void changed(ObservableValue<? extends Number> source, Number oldWidth, Number newWidth)
            {
                TableHeaderRow header = (TableHeaderRow) tableViewSong.lookup("TableHeaderRow");
                header.reorderingProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                        header.setReordering(false);
                    }
                });
            }
        });

        //设置表格列的宽度随这个borderPane的宽度而动态改变
        borderPane.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                nameColumn.setPrefWidth(newValue.doubleValue()/6.5*2);
                singerColumn.setPrefWidth(newValue.doubleValue()/6.5*1);
                albumColumn.setPrefWidth(newValue.doubleValue()/6.5*1.5);
                totalTimeColumn.setPrefWidth(newValue.doubleValue()/6.5*1);
                sizeColumn.setPrefWidth(newValue.doubleValue()/6.5*1);
            }
        });

        File CHOSE_FOLDER_FILE = new File("src" + File.separator + "main" + File.separator + "resources" + File.separator + "config" + File.separator + "chose-folder.xml");
        if (CHOSE_FOLDER_FILE.exists()){
            List<String> folderList = XMLUtils.getAllRecord(CHOSE_FOLDER_FILE,"Folder","path");
            if (folderList.size()>0){

//                Platform.runLater(()->{
//                    System.out.println("loading");
//                    LoadingSongService loadingSongService = applicationContext.getBean(LoadingSongService.class);
//                    progressIndicator.visibleProperty().bind(loadingSongService.runningProperty());
//                    tableViewSong.itemsProperty().bind(loadingSongService.valueProperty());
//                    loadingSongService.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
//                        @Override
//                        public void handle(WorkerStateEvent event) {
//                            System.out.println("success");
//                        }
//                    });
//                    loadingSongService.start();
//                    loadingSongService.setOnFailed(event -> System.out.println("fail"));
//                    loadingSongService.setOnCancelled(event -> System.out.println("cancel"));
//                });

                ObservableList<Song> songObservableList = SongUtils.getObservableSongList(folderList);
                tableViewSong.setItems(songObservableList);
                labSongCount.setText(String.valueOf(songObservableList.size()));
            }

        }



        /*******Fixed some resize bug here.*/
        localMusicContentContainer.setCursor(Cursor.DEFAULT);
        /*******Fixed some resize bug here.*/
    }

    /**“选择目录”按钮按下事件处理*/
    @FXML
    public void onClickedChoseFolder(MouseEvent mouseEvent) throws IOException {
        if (mouseEvent.getButton() == MouseButton.PRIMARY){  //鼠标左击
            FXMLLoader fxmlLoader = applicationContext.getBean(SpringFXMLLoader.class).getLoader("/fxml/chose-musicfolder.fxml");  //获取被Spring工厂接管的FXMLLoader对象
            Stage choseFolderStage = StageUtils.getStage((Stage) hBoxChoseFolder.getScene().getWindow(),fxmlLoader.load());

            StageUtils.syncCenter((Stage) hBoxChoseFolder.getScene().getWindow(),choseFolderStage);   //设置addMusicGroupStage对象居中到primaryStage
            WindowUtils.blockBorderPane(mainController.getBorderPane());         //设置borderPane不响应鼠标事件和改变透明度

            choseFolderStage.showAndWait();  //显示并且等待
//            if (choseFolderController.loadProperty().getValue()){  //如果是按下了“确定”按钮
//                //逻辑乱了的部分，先实现吧。
//
////                LoadingSongService loadingSongService = new LoadingSongService();
////                LoadingSongService loadingSongService = applicationContext.getBean(LoadingSongService.class);
////                progressIndicator.visibleProperty().bind(loadingSongService.runningProperty());
////                vBoxSongContainer.visibleProperty().bind(loadingSongService.valueProperty());
////                loadingSongService.start();
//            }
        }
    }

    /**单击"歌曲"标签的事件处理*/
    public void onClickedHBoxSong(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() == MouseButton.PRIMARY){
            this.setSelectedTab(hBoxSong);
        }
    }



    /**单击"歌手"标签的事件处理*/
    public void onClickedHBoxSinger(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() == MouseButton.PRIMARY){
            this.setSelectedTab(hBoxSinger);
        }
    }

    /**单击"专辑"标签的事件处理*/
    public void onClickedHBoxAlbum(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() == MouseButton.PRIMARY){
            this.setSelectedTab(hBoxAlbum);
        }
    }

    /**设置选择的标签背景颜色的函数*/
    private void setSelectedTab(HBox selectedTab){
        //首先重置所有的标签的背景颜色，我这里的HBox标签背景颜色是由另外一个HBox包裹做背景颜色显示的，所以需要getParent，设置parent的样式
        for (HBox hBoxTab:tabList){
            hBoxTab.getStyleClass().remove("selectedHBox");  //移除parent的css类名
        }
        //然后给当前选中的标签的parent容器添加css类名
        selectedTab.getStyleClass().add("selectedHBox");
    }

    /**单机表格存储歌曲的容器事件处理*/
    @FXML
    public void onClickedTableView(MouseEvent mouseEvent) throws Exception{
        if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() == 2){  //鼠标双击执行
            myMediaPlayer.play(tableViewSong.getSelectionModel().getSelectedItem());      //播放选中的歌曲
            myMediaPlayer.setPlaySongList(tableViewSong.getItems());     //设置当前播放列表
            myMediaPlayer.setCurrentPlayIndex(tableViewSong.getSelectionModel().getFocusedIndex());  //设置当前播放的歌曲在表格中的位置
            //设置右下角"歌单文本提示"显示数量
            if (String.valueOf(myMediaPlayer.getPlaySongList().size()).length()>=3){  //如果文本长度大于等于3,直接显示99.控制文本长度为2位数
                bottomController.getLabPlayListCount().setText("99");
            }
            else {  //否则,设置为播放列表的大小
                bottomController.getLabPlayListCount().setText(String.valueOf(myMediaPlayer.getPlaySongList().size()));
            }
        }
    }

    /**"播放全部"按钮的鼠标单击事件处理*/
    @FXML
    public void onClickedPlayAll(MouseEvent mouseEvent) throws TagException, ReadOnlyFileException, CannotReadException, InvalidAudioFrameException, IOException {
        if (mouseEvent.getButton() == MouseButton.PRIMARY){
            if (myMediaPlayer.getPlayMode() == PlayMode.SHUFFLE){   //如果当前播放模式为"随机播放"
                //生成一个随机数，执行播放
                int randomIndex=new Random().nextInt(tableViewSong.getItems().size());
                myMediaPlayer.setCurrentPlayIndex(randomIndex);     //设置当前播放的索引为生成的随机索引
                myMediaPlayer.play(tableViewSong.getItems().get(myMediaPlayer.getCurrentPlayIndex()));      //播放生成的随机索引歌曲

            }
            else {  //否则,不是"随机播放"模式,这些都是播放表格中的第一首歌曲
                myMediaPlayer.setCurrentPlayIndex(0);  //设置当前播放的歌曲为表格第一首歌曲
                myMediaPlayer.play(tableViewSong.getItems().get(0));      //播放表格中第一首歌曲
            }
            myMediaPlayer.setPlaySongList(tableViewSong.getItems());     //设置当前播放列表
            //设置右下角"歌单文本提示"显示数量
            if (String.valueOf(myMediaPlayer.getPlaySongList().size()).length()>=3){  //如果文本长度大于等于3,直接显示99.控制文本长度为2位数
                bottomController.getLabPlayListCount().setText("99");
            }
            else {  //否则,设置为播放列表的大小
                bottomController.getLabPlayListCount().setText(String.valueOf(myMediaPlayer.getPlaySongList().size()));
            }
        }
    }


//    /**中间面板的鼠标移动事件，修复鼠标形状*/
//    @FXML
//    public void onBorderPaneMouseMoved(MouseEvent mouseEvent) {
//
////        localMusicContentContainer.setCursor(Cursor.DEFAULT);
//    }
}
