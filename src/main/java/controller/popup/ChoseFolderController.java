package controller.popup;

import controller.content.LocalMusicContentController;
import controller.main.MainController;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import mediaplayer.Config;
import mediaplayer.MyMediaPlayer;
import org.dom4j.DocumentException;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Controller;
import service.LoadLocalSongService;
import util.ListUtils;
import util.WindowUtils;
import util.XMLUtils;
import javax.annotation.Resource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

@Controller
public class ChoseFolderController {

    /**右上角"关闭"图标*/
    @FXML
    private Label labCloseIcon;

    /**"将自动扫描您勾选的目录。"Label组件*/
    @FXML
    private Label labTips;

    /**存放组件checkbox的容器，CheckBox为选择的目录路径显示组件*/
    @FXML
    private VBox vWrapCheckBox;

    /**注入窗体根容器（BorderPane）的中间容器的控制器*/
    @Resource
    private MainController mainController;

    /**注入“本地音乐”面板的控制类*/
    @Resource
    private LocalMusicContentController localMusicContentController;

    /**记录存储文件保存的音乐目录的字符串集合*/
    private List<String> folderPathList;

    /**记录ＵＩ界面选择的目录的字符串集合*/
    private List<String> selectedPaths;

    /**注入Spring上下文对象*/
    @Resource
    private ConfigurableApplicationContext applicationContext;

    /**注入自定义的播放器对象*/
    @Resource
    private MyMediaPlayer myMediaPlayer;

    public void initialize() throws IOException, DocumentException {

        File choseFolderConfigFile = applicationContext.getBean(Config.class).getChoseFolderConfigFile();
        if (!choseFolderConfigFile.exists()){  //如果文件不存在，创建文件
            choseFolderConfigFile.createNewFile();                          //创建XML文件
            XMLUtils.createXML(choseFolderConfigFile,"FolderList");//添加根元素
        }
        else {   //文件存在，读取记录
            folderPathList =  XMLUtils.getAllRecord(choseFolderConfigFile,"Folder","path");
            if (folderPathList != null && folderPathList.size() > 0){
                for (String folderPath:folderPathList){
                    CheckBox checkBox = new CheckBox(folderPath);  //创建CheckBox组件
                    checkBox.getStylesheets().add("css/CheckBoxStyle.css"); //添加样式
                    checkBox.setSelected(true);
                    vWrapCheckBox.getChildren().add(checkBox);     //添加组件
                }
            }
        }

    }

    /**右上角关闭图标的事件处理*/
    @FXML
    public void onClickedCloseIcon(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() == MouseButton.PRIMARY){  //鼠标左击
            ((Stage)labCloseIcon.getScene().getWindow()).close();      //关闭窗口
            WindowUtils.releaseBorderPane(mainController.getBorderPane());
        }
    }

    /**”确定“按钮的事件处理*/
    @FXML
    public void onConfirmAction(ActionEvent actionEvent) throws Exception {

        //先删除原先的存储文件，然后再重新创建新文件。
        File choseFolderConfigFile = applicationContext.getBean(Config.class).getChoseFolderConfigFile();
        if (choseFolderConfigFile.exists()){    //如果文件已经存在，先删除它
            choseFolderConfigFile.delete();
        }
        choseFolderConfigFile.createNewFile();                          //创建XML文件
        XMLUtils.createXML(choseFolderConfigFile,"FolderList");//添加根元素
        ObservableList<Node> observableList = vWrapCheckBox.getChildren();  //获取vWrapCheckBox的子组件
        observableList.remove(labTips);  //移除用作提示的label组件

        selectedPaths = new ArrayList<>();  //存储选择的目录集合
        for (Node node:observableList){  //遍历所有的checkbox集合
            CheckBox checkBox = (CheckBox)node;
            if (checkBox.isSelected()){
                selectedPaths.add(checkBox.getText());
            }
        }
        for(String pathValue:selectedPaths){  //遍历逐个目录路径保存
            XMLUtils.addOneRecord(choseFolderConfigFile,"Folder","path",pathValue);
        }

        onClickedCloseIcon(new MouseEvent(MouseEvent.MOUSE_CLICKED, 0,
                0, 0, 0, MouseButton.PRIMARY, 1, true, true, true, true,
                true, true, true, true, true, true, null)); //关闭窗口

        if (!ListUtils.checkWeatherSame(selectedPaths,folderPathList)){   //如果不一样，证明更改了目录，重新加载目录下的歌曲文件
            System.out.println("need to load song");
            LoadLocalSongService loadLocalSongService = applicationContext.getBean(LoadLocalSongService.class);
            localMusicContentController.getProgressIndicator().visibleProperty().bind(loadLocalSongService.runningProperty());
            localMusicContentController.getTableViewSong().itemsProperty().bind(loadLocalSongService.valueProperty());
            loadLocalSongService.restart();

            //因为需要重新加载歌曲,所以需要判断是否播放器有歌曲正在播放中
            if (myMediaPlayer.getPlayer()!=null){  //如果媒体播放器对象存在,销毁它
                myMediaPlayer.destroy();    //销毁
                localMusicContentController.getTabPane().getTabs().get(0).setText("0"); //更新显示歌曲数目的label组件
            }
        }

    }

    /**"添加文件夹"按钮的事件处理*/
    @FXML
    public void onAddFolderAction(ActionEvent actionEvent) {
        DirectoryChooser directoryChooser = new DirectoryChooser();  //创建目录选择舞台
        directoryChooser.setInitialDirectory(new File(System.getProperties().getProperty("user.home")));
        File directory = directoryChooser.showDialog(labCloseIcon.getScene().getWindow());  //设置文件目录框的owner
        if(directory!=null) {  //选择了目录
            String folderPath=directory.getPath();  //提取目录的路径
            ObservableList<Node> observableList = vWrapCheckBox.getChildren();  //获取vWrapCheckBox的所有子组件
            for (Node node : observableList){  //如果observableList中已经有选择的folderPath，直接返回，不做处理
                if (node instanceof CheckBox){
                    if (((CheckBox)node).getText().equals(folderPath)){
                        return;
                    }
                }
            }
            CheckBox checkBox = new CheckBox(folderPath);  //创建CheckBox组件
            checkBox.getStylesheets().add("css/CheckBoxStyle.css"); //添加样式
            checkBox.setSelected(true);
            vWrapCheckBox.getChildren().add(checkBox);     //添加组件
        }
    }

}
