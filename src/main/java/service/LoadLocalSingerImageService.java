package service;

import com.alibaba.fastjson.JSON;
import controller.content.LocalMusicContentController;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import mediaplayer.Config;
import model.LocalSinger;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import pojo.Singer;
import util.HttpClientUtils;
import util.ImageUtils;
import util.SongUtils;
import javax.annotation.Resource;
import java.nio.charset.Charset;
import java.util.List;

/**
 * @author super lollipop
 * @date 20-2-9
 */
@Service
@Scope("prototype")
public class LoadLocalSingerImageService extends javafx.concurrent.Service<Void> {

    @Resource
    private LocalMusicContentController localMusicContentController;

    @Resource
    private Config config;

    @Override
    protected Task<Void> createTask() {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                //获取歌手表格所有的歌手集合singerList
                ObservableList<LocalSinger> observableLocalSingerList = localMusicContentController.getTableViewSinger().getItems();
                String url = config.getSingerURL() + "/queryByName/";
                for (int i = 0; i < observableLocalSingerList.size(); i++) {
                    Label labSinger = observableLocalSingerList.get(i).getLabSinger();
                    if (!SongUtils.isCharacterCategory(labSinger.getText())){   //如果不是字母分类行
                        try {
                            /**歌手图片*/
                            MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create().addTextBody("name",labSinger.getText(), ContentType.create("text/plain", Charset.forName("utf-8")));
                            String responseString = HttpClientUtils.executePost(url,multipartEntityBuilder.build());
                            List<Singer> singerList = JSON.parseArray(responseString, Singer.class);
                            if (singerList != null && singerList.size() > 0){    //查询得到的歌手集合不为空
                                Singer singer = singerList.get(0);  //取查询得到的第一个，因为可能存在同名的歌手
                                Platform.runLater(()->{
                                    Image imageSinger = new Image(singer.getImageURL(),48,48,false,true); //根据查询得到的url创建图片对象
                                    if (!imageSinger.isError()){ //如果没错误，设置歌手图片
                                        labSinger.setGraphic(ImageUtils.createImageView(imageSinger,48,48));    //设置Label显示图片
                                    }
                                });
                            }
                        }catch (HttpHostConnectException e){
                            System.out.println("无网络连接，加载歌手图片任务取消");
                            break;
                        }catch (Exception exception){
                            System.out.println(labSinger.getText() + " cause exception.");
                        }
                    }
                }

                return null;
            }
        };
        return task;
    }
}
