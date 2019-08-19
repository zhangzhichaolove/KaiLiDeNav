/*
 * @Title CldNavigatorActivity.java
 * @Copyright Copyright 2010-2015 Careland Software Co,.Ltd All Rights Reserved.
 * @author Huagx
 * @date 2015-11-10 下午7:20:16
 * @version 1.0
 */
package peak.chao.kailide;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import com.cld.log.CldLog;
import com.cld.mapapi.map.MapView;
import com.cld.navisdk.CldNaviManager;
import com.cld.navisdk.guide.BaseNavigorView;
import com.cld.navisdk.guide.BaseNavigorView.OnStopListener;
import com.cld.navisdk.guide.CldNavigatorView;
import com.cld.navisdk.guide.IOnGuideListener;
import com.cld.navisdk.routeguide.CldNaviConfig;
import com.cld.navisdk.routeguide.CldNavigator;
import com.cld.navisdk.routeinfo.CldRouteGuideManager;
import com.cld.navisdk.routeinfo.RouteLineInfo;
import com.cld.navisdk.util.view.CldPromptDialog;
import com.cld.navisdk.util.view.CldPromptDialog.PromptDialogListener;
import com.cld.nv.guide.CldHudInfo;
import com.cld.nv.guide.CldHudModel.HudGuide;

/** 
 * 导航界面
 * @author Huagx
 * @date 2015-11-10 下午7:20:16
 */
public class NavigatorActivity extends Activity {
	BaseNavigorView navigatorView;
	MapView nMapView;
	/**
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//创建mapView
		nMapView = CldNaviManager.getInstance().createNMapView(this);

//		nMapView.getMap().getUiSetting().showMinMap(true).setMinMapRect(100,300,300,300);
		//创建导航视图
		boolean isRelNavi = getIntent().getExtras().getBoolean(CldNaviConfig.KEY_NAVIMODEL_RELNAVI);
		Bundle bundle = new Bundle();
		//传入导航类型参数
		bundle.putBoolean(CldNaviConfig.KEY_NAVIMODEL_RELNAVI, isRelNavi);
		//初始化导航控件
		if(isRelNavi){
			CldNavigatorView cldNavigatorView = CldNavigator.getInstance().init(this, nMapView);
			cldNavigatorView.setAutoFinish(false);
			navigatorView = cldNavigatorView;
		}else{
			navigatorView = CldNavigator.getInstance().initSimulation(this, nMapView);
		}

//		navigatorView.showMinMap(true);
//		navigatorView.setMinMapRect(500,300,300,300);

		navigatorView.setOnStopListener(new OnStopListener() {

			@Override
			public void onStop() {
				// TODO Auto-generated method stub

			}

			@Override
			public boolean onBeforeStop() {
				// TODO Auto-generated method stub
				CldPromptDialog.createPromptDialog(NavigatorActivity.this, "退出", "确定停止导航?", "确定", "取消",
						new PromptDialogListener() {

							@Override
							public void onSure() {
								navigatorView.stopNavi();// 调用导航模式相应回退方法
							}

							@Override
							public void onCancel() {

							}
						});
				return true;
			}
		});
		navigatorView.setFocusable(true);
		//填充视图
		setContentView(navigatorView);
		//开始导航
		CldNavigator.getInstance().startNavi();
		//让屏幕保持不暗不关闭
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		navigatorView.setOnGuideListener(new IOnGuideListener() {

			@Override
			public void onYaWingPlanSuccess() {
				// TODO Auto-generated method stub
				Log.e("NavigatorActivity", "onYaWingPlanSuccess");
				RouteLineInfo routeLineInfo = CldRouteGuideManager.getInstance().getDrivingRouteDetail();
				CldLog.v("CLDLOG","routelineInfo:"+routeLineInfo);
			}

			@Override
			public void onYaWingPlanStart() {
				// TODO Auto-generated method stub
				Log.e("NavigatorActivity", "onYaWingPlanStart");
			}

			@Override
			public void onYaWingPlanFail(int errCode) {
				// TODO Auto-generated method stub
				Log.e("NavigatorActivity", "onYaWingPlanFail");
			}

			@Override
			public void onOverSpeed(int speed) {
				// TODO Auto-generated method stub
				Log.e("NavigatorActivity", "onOverSpeed");
			}

			@Override
			public void onHudUpdate(CldHudInfo hudInfo) {
				// TODO Auto-generated method stub
				if (hudInfo != null && hudInfo.getHudGuide() != null) {
					HudGuide guide = hudInfo.getHudGuide();
					if(guide != null)
					Log.e("NavigatorActivity", "onHudUpdate:" + guide.roadName
							+ " 剩余距离：" + guide.remDistance + " 剩余时间："
							+ guide.remTime + "\n"
							+ hudInfo.getHudTTS().voiceText);
				}
			}

			@Override
			public void onCityChange(String startCityName, String destCityName) {
				// TODO Auto-generated method stub
				Log.e("NavigatorActivity", "onCityChange 从" + startCityName
						+ "进入" + destCityName);
			}

			@Override
			public void onArrivePass(Object pass) {
				// TODO Auto-generated method stub
				Log.e("NavigatorActivity", "onArrivePass");
			}

			@Override
			public void onArriveDestNear() {
				// TODO Auto-generated method stub
				Log.e("NavigatorActivity", "onArriveDestNear");
			}

			@Override
			public void onArriveDest() {
				// TODO Auto-generated method stub
				Log.e("NavigatorActivity", "onArriveDest");
			}

//			@Override
//			public void onNotifyAvoidRoute(AcirtRouteInfo acirtRouteInfo) {
//
//			}
		});

//		if(CldNaviUtil.isCountingDistance()){
//			CldNaviUtil.continueCountDistance();
//		}else{
//			CldNaviUtil.startCountDistance();
//		}

	}

	/**
	 * @see android.app.Activity#onBackPressed()
	 */
	public void onBackPressed() {
		 CldNavigator.getInstance().onBackPressed();//调用导航模式相应回退方法
	}

	/**
	 * @see android.app.Activity#onStop()
	 */
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	@Override
	public void onPause() {
		super.onPause();
		if (null != nMapView) {
			nMapView.onPause();//当地图控件存在，调用地图控件暂停方法

		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (null != nMapView) {
			nMapView.onResume();//当地图控件存在，调用地图控件恢复方法
			nMapView.update();//同时更新地图控件的状态
		}
		CldNavigator.getInstance().onResume();
	}

	/**
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (null != nMapView) {
			nMapView.destroy();//当地图控件存在时，销毁地图控件
		}
	}
}
