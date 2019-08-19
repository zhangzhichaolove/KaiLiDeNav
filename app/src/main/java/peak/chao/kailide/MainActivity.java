package peak.chao.kailide;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.cld.mapapi.account.CldAccountAPI;
import com.cld.mapapi.map.CldMap;
import com.cld.mapapi.map.MapView;
import com.cld.navisdk.CldNaviManager;
import com.cld.navisdk.hy.routeplan.CldHYRoutePlaner;
import com.cld.navisdk.hy.routeplan.HYRoutePlanParm;
import com.cld.navisdk.hy.utils.CldTruckUtil;
import com.cld.navisdk.routeguide.CldNaviConfig;
import com.cld.navisdk.routeplan.CldRoutePlaner;
import com.cld.navisdk.routeplan.RoutePlanNode;
import com.cld.navisdk.util.view.CldProgress;
import com.cld.ols.module.account.CldKAccountAPI;

public class MainActivity extends AppCompatActivity {
    private MapView mMapView = null;
    // 路径规划失败标识
    private final int MSG_ID_PLANROUTE_FAILED = 1001;
    // 路径规划成功标识
    private final int MSG_ID_PLANROUTE_SUCCESS = MSG_ID_PLANROUTE_FAILED + 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 初始化sdk
        CldNaviManager.getInstance().init(this, mInitListener);
    }

    // 监听key是否校验成功,并给出提示.
    private CldMap.NaviInitListener mInitListener = new CldMap.NaviInitListener() {

        @Override
        public void initFailed(String msg) {// 初始化失败方法

        }

        @Override
        public void onAuthResult(int status, String msg) {// 初始化结果
            String str;
            if (0 == status) {// 初始化结果状态判断
                str = "key校验成功!";
            } else {
                str = "key校验失败!";
            }
            // 初始化结果返回的信息提示
            Toast.makeText(MainActivity.this, str, Toast.LENGTH_LONG)
                    .show();

            CldAccountAPI.getInstance().loginYQ("13012259076", "LFPZ1APC7J5A12468",
                    new CldKAccountAPI.ICldKAccountListener() {

                        @Override
                        public void onLoginResult(int errorCode, String errorMsg) {
                            Log.v("CLDLOG", "login errorCode:" + errorCode
                                    + ",errorMsg:" + errorMsg);
                        }
                    });
//			CldNaviEngineManager.getInstance().setOnLocationChangeListener(
//					new ILocationChangeListener() {
//
//						@Override
//						public void onLocationChanged(LatLng latLng, long time,
//								float speed) {
//							CldLog.v("CLDLOG", "onLocationChanged:"
//									+ latLng.longitude + "," + latLng.latitude);
//							Toast.makeText(DemoMainActivity.this, "定位更新", Toast.LENGTH_SHORT).show();
//						}
//					});
//			CldDeviceAPI.setLocUploadInterval(3, 18);

        }

        @Override
        public void initStart() {// 初始化开始方法

        }

        @Override
        public void initSuccess() {// 初始化成功方法
            mMapView = CldNaviManager.getInstance().createNMapView(MainActivity.this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            ((LinearLayout) findViewById(R.id.root)).addView(mMapView, params);
            startCalcRoute();
        }
    };


    // 消息处理
    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_ID_PLANROUTE_FAILED:// 失败
                    Toast.makeText(MainActivity.this,
                            msg.getData().getString("info"), Toast.LENGTH_LONG)
                            .show();
                    break;

                case MSG_ID_PLANROUTE_SUCCESS:// 成功
                    int avoidNum = CldTruckUtil.getAvoidLimitNum();// 货运目前为单路径，直接获取
                    int limitNum = CldTruckUtil.getLimitNum();
                    Toast.makeText(
                            MainActivity.this,
                            "路径规划成功,已为您回避" + avoidNum + "个限行,还有" + limitNum + "个限行",
                            Toast.LENGTH_LONG).show();
                    startNavi(true);
                    break;
            }
        }

        ;
    };

    /**
     * 开始规划路线
     *
     * @return void
     * @author Zhangjb
     * @date 2016-6-21 上午11:33:25
     */
    private void startCalcRoute() {
        double sX = 0, sY = 0, eX = 0, eY = 0;
        try {
            // String转成double
            sX = Double.parseDouble("114.1158056");
            sY = Double.parseDouble("22.60075");
            eX = Double.parseDouble("114.152194");
            eY = Double.parseDouble("22.608361");
        } catch (Exception e) {
            e.printStackTrace();
        }

        float mWidth = 0.0f; // 单位:m
        float mHeight = 0.0f; // 单位:m
        float mWeight = 0.0f; // 单位:吨
        try {
            // String转成float
            mWidth = Float.parseFloat("2.8");//宽度（米）
            mHeight = Float.parseFloat("4.2");//高度（米）
            mWeight = Float.parseFloat("13.5");//重量（吨）
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (mWidth <= 0 || mHeight <= 0 || mWeight <= 0) {
            Toast.makeText(this, "请先设置货车参数", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mWidth > 9.9) {
            Toast.makeText(this, "宽度不能大于9.9米", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mHeight > 9.9) {
            Toast.makeText(this, "高度不能大于9.9米", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mWeight > 99.99) {
            Toast.makeText(this, "重量不能大于99.99吨", Toast.LENGTH_SHORT).show();
            return;
        }
        // 显示等待进度条
        CldProgress.showProgress(MainActivity.this, "正在规划路线...",
                new CldProgress.CldProgressListener() {
                    @Override
                    public void onCancel() {

                    }
                });

        // 起点
        RoutePlanNode startNode = new RoutePlanNode(sY, sX, "创建大厦",
                "深圳市福田区深南大道6023号", RoutePlanNode.CoordinateType.CLD);
        // 终点
        RoutePlanNode endNode = new RoutePlanNode(eY, eX, "深圳市政府",
                "深圳市福田区福中三路", RoutePlanNode.CoordinateType.CLD);
        // 设置货车参数 车宽，车高，车重，是否忽略限行，是否重量参与路线规划
        HYRoutePlanParm hyRoutePlanParm = new HYRoutePlanParm();
        hyRoutePlanParm.width = mWidth;
        hyRoutePlanParm.height = mHeight;
        hyRoutePlanParm.weight = mWeight;
        hyRoutePlanParm.isIgnoreLimit = true;// 设置是否忽略限行
        //        <item>重量不参与计算不提醒</item>
        //        <item>重量不参与计算提醒</item>
        //        <item>重量参与计算</item>
        hyRoutePlanParm.weightFlag = HYRoutePlanParm.HYWeightFlag.NO_WEIGHT_TO_PLAN_NO_PLAY;
        //        <item>2轴</item>
        //        <item>3轴</item>
        //        <item>4轴</item>
        //        <item>5轴</item>
        //        <item>6轴及以上</item>
        //    </string-array>
        hyRoutePlanParm.axleCount = 2;
        //        <item>微型车辆</item>
        //        <item>轻型车辆</item>
        //        <item>中型车辆</item>
        //        <item>重型车辆</item>
        hyRoutePlanParm.truckType = HYRoutePlanParm.HYTruckType.MiniTruck;
        // 路径规划监听器
        CldRoutePlaner.RoutePlanListener listener = new CldRoutePlaner.RoutePlanListener() {
            @Override
            public void onRoutePlanSuccessed() {// 规划成功
                handler.sendEmptyMessage(MSG_ID_PLANROUTE_SUCCESS);
            }

            @Override
            public void onRoutePlanFaied(int err, String info) {// 规划失败
                Message message = Message.obtain();
                message.what = MSG_ID_PLANROUTE_FAILED;
                Bundle bundle = new Bundle();
                bundle.putString("info", info);
                message.setData(bundle);
                handler.sendMessage(message);
            }

            @Override
            public void onRoutePlanCanceled() {

            }
        };
        // 调用货运规划接口
        CldHYRoutePlaner.getInstance().hyRoutePlan(MainActivity.this,
                startNode, // 起点
                null, // 经由地
                endNode, // 终点
                //        int ROUTE_PLAN_MOD_RECOMMEND = 1;//路线计划建议
                //        int ROUTE_PLAN_MOD_MIN_TIME = 2;//路线最小时间
                //        int ROUTE_PLAN_MOD_MIN_DIST = 8;//线路最小距离
                //        int ROUTE_PLAN_MOD_MIN_TOLL = 16;//线路最小伤亡人数
                //        int ROUTE_PLAN_MOD_AVOID_TAFFICJAM = 64;//线路平面图避开系数
                //        int ROUTE_PLAN_MOD_WALK = 32;//线路平面图步行
                //        int ROUTE_PLAN_MOD_FERRY = 128;//渡船
                CldNaviManager.RoutePlanPreference.ROUTE_PLAN_MOD_FERRY, // 算路方式
                hyRoutePlanParm, listener);
    }

    /**
     * 开启导航方法
     *
     * @return void
     * @author Zhangjb
     * @date 2016-6-21 上午11:43:12
     */
    private void startNavi(boolean isReal) {//是否模拟导航
        if (!CldRoutePlaner.getInstance().hasPlannedRoute()) {// 判断是否有规划路径成功
            Toast.makeText(this, "请先规划路径！", Toast.LENGTH_LONG).show();
            return;
        }
        // 路径规划成功，则跳转到导航界面
        Intent intent = new Intent(MainActivity.this,
                NavigatorActivity.class);
        intent.putExtra(CldNaviConfig.KEY_NAVIMODEL_RELNAVI, isReal);
        startActivity(intent);
    }

    @Override
    // 点击返回键的事件处理
    public void onBackPressed() {
        // 退出程序
        System.exit(0);
        // 杀死进程
        android.os.Process.killProcess(android.os.Process.myUid());
        // sdk反初始化
        CldNaviManager.getInstance().unInit();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (null != mMapView) {
            mMapView.onPause();// 当地图控件存在时，调用相应的生命周期方法
            CldTruckUtil.removeTruckOverlays();
        }
    }

    @Override
    protected void onDestroy() {
        CldRoutePlaner.getInstance().clearRoute(); // 关闭界面时，清除规划路线
        super.onDestroy();
    }
}
