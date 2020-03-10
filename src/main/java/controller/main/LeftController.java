package controller.main;

import controller.component.GroupTabController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import mediaplayer.Config;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import application.SpringFXMLLoader;
import pojo.Group;
import pojo.User;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

@Controller
public class LeftController {

    /**左侧”搜索“标签Tab*/
    @FXML
    private HBox hBoxSearchTab;

    /**左侧”发现音乐“标签Tab*/
    @FXML
    private HBox hBoxExploreMusicTab;

    /**左侧本地音乐标签Tab*/
    @FXML
    private HBox hBoxLocalMusicTab;

    /**左侧"最近播放"标签Tab*/
    @FXML
    private HBox hBoxRecentPlayTab;

    /**装标签的集合tabList*/
    private List<HBox> tabList;

    /**存储标签的VBox容器*/
    @FXML
    private VBox vBoxTabContainer;

    /**显示用户头像的Label组件*/
    @FXML
    private Label labUserImage;

    /**显示用户名称的Label组件*/
    @FXML
    private Label labUserName;

    /**注入窗体根容器（BorderPane）的控制类*/
    @Resource
    private MainController mainController;

    /**注入窗体根容器（BorderPane）的中间容器的控制器*/
    @Resource
    private CenterController centerController;

    /**注入Spring上下文工具类*/
    @Resource
    private ApplicationContext applicationContext;

    /**"本地音乐"标签的内容容器*/
    private Parent localMusicParent;

    /**记录选择的标签索引*/
    private int selectedTabIndex = -1;

    public VBox getVBoxTabContainer() {
        return vBoxTabContainer;
    }

    public List<HBox> getTabList() {
        return tabList;
    }

    public Label getLabUserImage() {
        return labUserImage;
    }

    public Label getLabUserName() {
        return labUserName;
    }

    public Parent getLocalMusicParent() {
        return localMusicParent;
    }

    public int getSelectedTabIndex() {
        return selectedTabIndex;
    }

    public void initialize() throws IOException {

        tabList = new LinkedList<>();
        tabList.add(hBoxSearchTab);
        tabList.add(hBoxExploreMusicTab);
        tabList.add(hBoxLocalMusicTab);
        tabList.add(hBoxRecentPlayTab);

        this.setSelectedTab(hBoxExploreMusicTab);   //初始化“发现音乐”为选择的标签

        User user = applicationContext.getBean(Config.class).getUser();
        if (user != null){
            labUserName.setText(applicationContext.getBean(Config.class).getUser().getName());  //设置用户名称*/
        }

        labUserImage.setClip(new Circle(19,19,19)); //切割用户头像
    }

    /**添加歌单标签的函数
     * @param group 歌单对象*/
    public void addGroupTab(Group group) throws IOException {
        if (group.getFavor() == 1) { //如果是"我喜欢的音乐"歌单
            FXMLLoader fxmlLoader = applicationContext.getBean(SpringFXMLLoader.class).getLoader("/fxml/component/favorgroup-tab.fxml");    //"我喜欢的音乐"tab
            vBoxTabContainer.getChildren().add(fxmlLoader.load());
            GroupTabController groupTabController = fxmlLoader.getController();
            tabList.add(groupTabController.getHBoxGroup());
        } else {
            FXMLLoader fxmlLoader = applicationContext.getBean(SpringFXMLLoader.class).getLoader("/fxml/component/group-tab.fxml");    //"我喜欢的音乐"tab
            vBoxTabContainer.getChildren().add(fxmlLoader.load());
            GroupTabController groupTabController = fxmlLoader.getController();
            groupTabController.getLabGroupName().setText(group.getName());
            tabList.add(groupTabController.getHBoxGroup());
        }
        tabList.get(tabList.size() - 1).setUserData(group); //存储对象
    }

    /**根据歌单名称移除歌单标签的函数
     * @param tabName 歌单名称*/
    public void removeGroupTab(String tabName){
        for (int i = 5; i < tabList.size(); i++) {
            if (((Label)tabList.get(i).getChildren().get(0)).getText().equals(tabName)){  //如果歌单名称相等，移除
                vBoxTabContainer.getChildren().remove(tabList.get(i));
                tabList.remove(tabList.get(i));
            }
        }
    }

    /**当登录用户身份验证失败时，移除歌单tab组件的函数*/
    public void removeGroupTab(){
        vBoxTabContainer.getChildren().remove(5,vBoxTabContainer.getChildren().size() - 1);
        for (int i = 4; i < tabList.size(); i++) {
            tabList.remove(i);
        }
    }

    /**判断当前的歌单名称是否已经存在
     * @param tabName 歌单名称*/
    public boolean exist(String tabName){
        for (int i = 5; i < tabList.size(); i++) {
            if (((Label)tabList.get(i).getChildren().get(0)).getText().equals(tabName)){
                return true;
            }
        }
        return false;
    }

    /**获取目前选中的是哪一个标签的名称,如果没有找到，会抛出异常
     * */
    public HBox getContextMenuShownTab() throws Exception {
        int size = tabList.size();
        if (size >= 5){
            for (int i = 5; i < tabList.size(); i++) {
                if (tabList.get(i).isMouseTransparent()){
                    return tabList.get(i);
                }
            }
        }
        throw new Exception();
    }

    /**单击“搜索”标签事件处理*/
    @FXML
    public void onClickedSearchTab(MouseEvent mouseEvent) throws IOException {
        if (mouseEvent.getButton()== MouseButton.PRIMARY && getSelectedTab() != hBoxSearchTab){  //鼠标左击
            this.setSelectedTab(hBoxSearchTab);  //设置当前选择的为“搜索”标签
            FXMLLoader fxmlLoader = applicationContext.getBean(SpringFXMLLoader.class).getLoader("/fxml/content/tab-searchinput-content.fxml");
            centerController.getBorderPane().setCenter(fxmlLoader.load());
        }
    }

    /**单击“发现音乐”标签事件处理*/
    @FXML
    public void onClickedExplorerMusicTab(MouseEvent mouseEvent) {
        if (mouseEvent.getButton()== MouseButton.PRIMARY && getSelectedTab() != hBoxExploreMusicTab){  //鼠标左击
            this.setSelectedTab(hBoxExploreMusicTab);

            centerController.getBorderPane().setCenter(new Label("敬请期待"));

//            Slider slider = new Slider(0, 1, 0.5);
//            slider.setShowTickMarks(true);
//            slider.setShowTickLabels(true);
//            slider.setMajorTickUnit(0.2f);
//            slider.setContextMenu(null);
//            slider.setBlockIncrement(0.1f);
//            centerController.getBorderPane().setCenter(slider);
        }
    }

    /**单击“本地音乐”标签事件处理*/
    @FXML
    public void onClickedLocalMusicTab(MouseEvent mouseEvent) throws IOException {
        if (mouseEvent.getButton()== MouseButton.PRIMARY && getSelectedTab() != hBoxLocalMusicTab){  //鼠标左击
            this.setSelectedTab(hBoxLocalMusicTab);
            if (localMusicParent == null){
                FXMLLoader fxmlLoader = applicationContext.getBean(SpringFXMLLoader.class).getLoader("/fxml/content/tab-localmusic-content.fxml");
                localMusicParent = fxmlLoader.load();
            }
            centerController.getBorderPane().setCenter(localMusicParent);   //设置容器的中间容器内容
        }
    }

    /**单击“最近播放”标签事件处理*/
    @FXML
    public void onClickedRecentPlayTab(MouseEvent mouseEvent) throws IOException {
        if (mouseEvent.getButton()== MouseButton.PRIMARY && getSelectedTab() != hBoxRecentPlayTab){  //鼠标左击
            this.setSelectedTab(hBoxRecentPlayTab);
            FXMLLoader fxmlLoader = applicationContext.getBean(SpringFXMLLoader.class).getLoader("/fxml/content/tab-recentplay-content.fxml");
            centerController.getBorderPane().setCenter(fxmlLoader.load());
        }
    }

    /**设置选择的标签背景颜色的函数*/
    public void setSelectedTab(HBox selectedTab){
        this.resetSelectedTab();
        //然后给当前选中的标签的parent容器添加css类名
        selectedTab.getStyleClass().add("selectedHBox");
        selectedTabIndex = tabList.indexOf(selectedTab);
    }

    public HBox getSelectedTab(){
        return tabList.get(selectedTabIndex);
    }

    public void resetSelectedTab(){
        //重置所有的标签的背景颜色，我这里的HBox标签背景颜色是由另外一个HBox包裹做背景颜色显示的，所以需要getParent，设置parent的样式
        for (HBox hBoxTab:tabList){
            hBoxTab.getStyleClass().remove("selectedHBox");  //移除parent的css类名
        }
    }

    /**单击“用户头像”HBox容器的事件处理*/
    @FXML
    public void onClickedHBoxUserInfo(MouseEvent mouseEvent) throws IOException {
        if (mouseEvent.getButton() == MouseButton.PRIMARY){
            if (centerController.getStackPane().getChildren().size()==1){
                if (applicationContext.getBean(Config.class).getUser() == null){  //如果登录配置文件不存在，则没有登录
                    FXMLLoader fxmlLoader = applicationContext.getBean(SpringFXMLLoader.class).getLoader("/fxml/authentication/right-slide-unlogin.fxml");
                    BorderPane borderPaneRoot = fxmlLoader.load();

                    StackPane stackPane = centerController.getStackPane();
                    stackPane.getChildren().add(borderPaneRoot);  //添加进stackPane
                }
                else {  //如果登录对象存在，则已经登录过了。
                    FXMLLoader fxmlLoader = applicationContext.getBean(SpringFXMLLoader.class).getLoader("/fxml/authentication/right-slide-login.fxml");
                    BorderPane borderPaneRoot = fxmlLoader.load();

                    StackPane stackPane = centerController.getStackPane();
                    stackPane.getChildren().add(borderPaneRoot);  //添加进stackPane
                }
            }
        }
    }
}