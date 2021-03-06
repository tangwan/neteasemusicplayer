package controller.dialog;

import controller.main.MainController;
import controller.content.RecentPlayContentController;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import mediaplayer.Config;
import mediaplayer.MyMediaPlayer;
import model.RecentSong;
import org.springframework.stereotype.Controller;
import util.WindowUtils;
import javax.annotation.Resource;
import java.io.File;

/**
 * @author super lollipop
 * @date 20-2-5
 */
@Controller
public class ClearRecentPlayConfirmDialogController {

    /**
     * “取消”按钮组件
     */
    @FXML
    private Button btnCancel;

    /**
     * 注入窗体根容器（BorderPane）的控制类
     */
    @Resource
    private MainController mainController;

    /**
     * 注入最近播放内容的控制类
     */
    @Resource
    private RecentPlayContentController recentPlayContentController;

    /**注入自定义的媒体播放类*/
    @Resource
    private MyMediaPlayer myMediaPlayer;

    @Resource
    private Config config;

    /**
     * "确定"按钮的事件处理
     */
    @FXML
    public void onClickedConfirm(ActionEvent actionEvent) {
        File recentPlayFile = config.getRecentPlayFile();
        if (recentPlayFile.exists()){ //如果存储文件存在，删除
            recentPlayFile.delete();
        }
        ObservableList<RecentSong> recentSongs = recentPlayContentController.getTableViewRecentPlaySong().getItems();
        if (recentSongs != null && recentSongs.size() > 0){
            recentSongs.clear();    //清除最近播放表格的内容
            recentPlayContentController.getTabPane().getTabs().get(0).setText(String.valueOf(recentSongs.size()));    //更新歌曲数目显示
        }
        this.onClickedCancel(actionEvent);  //最后，调用“取消”按钮事件，关闭对话框
    }

    /**
     * “取消”按钮的事件处理
     */
    @FXML
    public void onClickedCancel(ActionEvent actionEvent) {
        btnCancel.getScene().getWindow().hide();  //隐藏窗口
        WindowUtils.releaseBorderPane(mainController.getBorderPane());
    }
}
