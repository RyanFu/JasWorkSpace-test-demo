package com.joyplus;

import java.text.SimpleDateFormat;
import java.util.Date;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import com.joyplus.mediaplayer.MediaInfo;
import com.joyplus.mediaplayer.VideoViewInterface.STATE;
import com.joyplus.tv.R;
import com.joyplus.tv.utils.UtilTools;

public class JoyplusMediaPlayerBar implements JoyplusMediaPlayerInterface{
    
	private boolean Debug = true;
	private String  TAG   = "JoyplusMediaPlayerBar";
	private JoyplusMediaPlayerActivity mActivity;
	private VideoViewController        mBottomBar;
	private VideoViewTopBar            mTopBar;

	private static final int MSG_BASE        = 300;
	private static final int MSG_SHOWVIEW    = MSG_BASE+1;
	private static final int MSG_HIDEVIEW    = MSG_BASE+2;
	//private static final int MSG_UPDATETIME  = MSG_BASE+3;
	private static final int MSG_REQUESTSHOW = MSG_BASE+4;
	private static final int MSG_REQUESTHIDE = MSG_BASE+5;
	private static final int LAYOUT_BAR      = MSG_BASE+7;

	public static final int  MSG_SHOWANDKEYLONGPRESS = MSG_BASE+8;
	public static final int  MSG_SHOWANDKEYDOWN      = MSG_BASE+9;
	public static final int  MSG_SHOWANDHOLD         = MSG_BASE+10;
	/*use to control seekbar*/
	enum SEEKMODE{
		NORMAL , LONGPRESS
	}
	enum SEEKTYPE{
		NORMAL , FORWARD , BACKWARD
	}
	enum SPEED{
		X0 ("x0"),X1 ("x1"), X2 ("x2"), X3 ("x3");
		private String speed;
		SPEED(String Speed){
			speed = Speed;
		}
		public String toString(){
			return speed;
		}
	}
	public int getIntSpeed(SPEED speed){
		if(speed == null)return 0;
		if(speed == SPEED.X3)return 4;
		if(speed == SPEED.X2)return 2;
		if(speed == SPEED.X1)return 1;
		if(speed == SPEED.X0)return 0;
		return 0;
	}
	public SPEED getNextSpeed(SPEED speed){
		if(speed == null)return null;
		if(speed == SPEED.X0){
			return SPEED.X1;
		}else if(speed == SPEED.X1){
			return SPEED.X2;
		}else if(speed == SPEED.X2){
			return SPEED.X3;
		}else if(speed == SPEED.X3){
			return SPEED.X3;
		}else{
			return SPEED.X0;
		}
	}
	public void Init(){
		mHandler.removeCallbacksAndMessages(null);
		mBottomBar.Init();
		mTopBar.Init();
	}
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch(msg.what){
			case JoyplusMediaPlayerActivity.MSG_MEDIAINFO:
				mBottomBar.dispatchMessage(msg);
				break;
			case MSG_SHOWVIEW:
				mTopBar.setVisible(true);
				mBottomBar.setVisible(true);
				setVisible(false,JoyplusMediaPlayerActivity.DELAY_SHOWVIEW);
				break;
			case MSG_HIDEVIEW:				
				mTopBar.setVisible(false);
				mBottomBar.setVisible(false);
				mHandler.removeCallbacksAndMessages(null);
				break;
			case MSG_REQUESTSHOW:
				//mHandler.removeCallbacksAndMessages(null);
				setVisible(true,500);
				//setVisible(false,JoyplusMediaPlayerActivity.DELAY_SHOWVIEW);
				break;
			case MSG_REQUESTHIDE:
				mHandler.removeCallbacksAndMessages(null);
				setVisible(false,0);
				break;
			case JoyplusMediaPlayerActivity.MSG_UPDATEPLAYERINFO:
				mTopBar.UpdatePlayerInfo();
				break;
			case MSG_SHOWANDKEYLONGPRESS:
			case MSG_SHOWANDKEYDOWN:
				mHandler.removeCallbacksAndMessages(null);
				mTopBar.setVisible(true);
				mBottomBar.setVisible(true);
				mBottomBar.dispatchMessage(msg);
				break;
			case MSG_SHOWANDHOLD:
				mTopBar.setVisible(true);
				mBottomBar.setVisible(true);
				mHandler.removeCallbacksAndMessages(null);
				break;
			}
		}
	};

	public JoyplusMediaPlayerBar(JoyplusMediaPlayerActivity context){
		mActivity  = context;
		mBottomBar = new VideoViewController();
		mTopBar    = new VideoViewTopBar();
		setVisible(true,0);
	}
	private void setVisible(boolean visible,int delay){
		Message m ;
		if(visible)
			m=Message.obtain(mHandler,MSG_SHOWVIEW,"MSG_SHOWVIEW");
		else
			m=Message.obtain(mHandler,MSG_HIDEVIEW,"MSG_HIDEVIEW");
		mHandler.removeCallbacksAndMessages("MSG_SHOWVIEW");
		mHandler.removeCallbacksAndMessages("MSG_HIDEVIEW");
		mHandler.sendMessageDelayed(m,delay);
	}

	/*add by Jas@20130812 for TopBar in JoyPlus VideoView
	 * it use to display Media name , media resolution .current time
	 * */
	private class VideoViewTopBar {

		private boolean     Debug = true;
		private String      TAG   = "VideoViewTopBar";

		private ImageView   MediaResolution;
		private TextView    MediaName;
		private TextView    Click;
		private RelativeLayout Layout; 
		public void Init(){
			Layout.setVisibility(View.VISIBLE);
		};
		public void UpdatePlayerInfo() {
			// TODO Auto-generated method stub
			if(Layout.getVisibility() == View.VISIBLE){
				 InitView();
			}
		}
		public VideoViewTopBar(){
			InitResource();
		}
		private Runnable UpdateTime = new Runnable(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(Layout.getVisibility() == View.VISIBLE)InitView();
			}
		};
		public void setVisible(boolean Visiblility){
			if(Debug)Log.d(TAG,"setVisibility("+Visiblility+")");
			//if(((Layout.VISIBLE == View.VISIBLE)?true:false) == Visiblility)return;
			Layout.setVisibility(Visiblility?View.VISIBLE:View.GONE);
			if(Layout.getVisibility() == View.VISIBLE){
				InitView();
				mHandler.removeCallbacks(UpdateTime);
				mHandler.postDelayed(UpdateTime, 1000);
			}else{
				Layout.startAnimation(JoyplusMediaPlayerActivity.mAlphaDispear);
				mHandler.removeCallbacks(UpdateTime);
			}
		}

		private void InitView() {
			// TODO Auto-generated method stub
			if(Layout.getVisibility() != View.VISIBLE)return;
			Date date = new Date();
			SimpleDateFormat format = new SimpleDateFormat("H:mm");
			Click.setText(format.format(date));
			MediaName.setText(JoyplusMediaPlayerActivity.mInfo.mPlayerName);
			if("1080p".equals(JoyplusMediaPlayerActivity.mInfo.mQua)){
				MediaResolution.setImageResource(R.drawable.player_1080p);
				MediaResolution.setVisibility(View.VISIBLE);
			}else if("720p".equals(JoyplusMediaPlayerActivity.mInfo.mQua)){
				MediaResolution.setImageResource(R.drawable.player_720p);
				MediaResolution.setVisibility(View.VISIBLE);
			}else{
				MediaResolution.setVisibility(View.GONE);
			}
		}

		private void InitResource() {
			// TODO Auto-generated method stub
			Layout          = (RelativeLayout) mActivity.findViewById(R.id.mediacontroller_topbar);
			MediaName       = (TextView)       mActivity.findViewById(R.id.mediacontroller_topbar_playname);
			MediaResolution = (ImageView)      mActivity.findViewById(R.id.mediacontroller_topbar_resolution);
			Click           = (TextView)       mActivity.findViewById(R.id.mediacontroller_topbar_time);
			setVisible(true);
		}
	}

	/*Add by Jas@20130813 for add the BottomBar in JoyPlus VideoView 
	 * it depends on the MediaInfo which report from JoyPlusMediaPlayerStateTrack
	 * and depends on the layout of joyplusvideoview.xml 
	 * */
	private class VideoViewController {
        
		private boolean     Debug = true;
		private String      TAG   = "VideoViewController";

		private TextView       CurrentTimeView;
		private TextView       TotalTimeView;
		private SeekBar        SeekBar;
		private RelativeLayout Layout_Time;
		private RelativeLayout Layout_seek;
		private RelativeLayout Layout_Speed;
		private TextView       SpeedView;
	    private int            DefaultSpeedSpace = 1000;
	    private int OFFSET = 33;
		private int seekBarWidthOffset  = 40;
		public void Init(){
			SeekBar.setEnabled(false);
			InitSpeed();
			setVisible(true);
			mHandler.removeCallbacks(QuickAdjustSeekBar);
			UpdateProgress(null);
			Layout_Time.setVisibility(View.VISIBLE);
		}
		public void InitSpeed(){
			mSeekBarMode = SEEKMODE.NORMAL;
			mSeekBarType = SEEKTYPE.NORMAL;
			mSpeed       = SPEED.X0;
		}
		private Runnable QuickAdjustSeekBar = new Runnable(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(mSeekBarType == SEEKTYPE.NORMAL) return;
				int position =0;
				if(mSeekBarType == SEEKTYPE.FORWARD)
				    position  = SeekBar.getProgress()+getSpeedSpace()*getIntSpeed(mSpeed);
				else if(mSeekBarType == SEEKTYPE.BACKWARD)
					position  = SeekBar.getProgress()-getSpeedSpace()*getIntSpeed(mSpeed);
				if(position<0)position = 0;
				if(position>SeekBar.getMax())position = SeekBar.getMax();
				SeekBar.setProgress(position);
				UpdateProgress(null);
				mHandler.sendEmptyMessage(MSG_REQUESTSHOW);
				mHandler.removeCallbacks(QuickAdjustSeekBar);
				mHandler.postDelayed(QuickAdjustSeekBar, 20);
			}
		};
		private int getSpeedSpace(){
			int Space = SeekBar.getMax();
			if(Space == 0)
				return 0;
			else 
				return Space/DefaultSpeedSpace;
		}
		/*it use to fast forward or fast backward*/
		private SEEKMODE mSeekBarMode;
		private SEEKTYPE mSeekBarType;
		private SPEED    mSpeed;
		public void dispatchMessage(Message m){
			switch(m.what){
			case JoyplusMediaPlayerActivity.MSG_MEDIAINFO:
				 if(Layout_Time.getVisibility() == View.VISIBLE){
					 UpdateProgress(((MediaInfo) m.obj).CreateMediaInfo());
				 }
				 break;
			case MSG_SHOWANDKEYLONGPRESS:
				JoyplusonKeyLongPress((Integer) m.obj,null);
				break;
			case MSG_SHOWANDKEYDOWN:
				JoyplusonKeyDown((Integer) m.obj,null);
				break;
			}
		}
		public boolean JoyplusonKeyLongPress(int keyCode, KeyEvent event){
			Log.d("KeyCode","Bar JoyplusonKeyLongPress keyCode="+keyCode);
			if(Layout_Time.getVisibility() == View.VISIBLE && (mActivity.getPlayer()!=null)){
				Log.d("KeyCode","Bar JoyplusonKeyLongPress ");
				switch(keyCode){
				case KeyEvent.KEYCODE_DPAD_LEFT:
					mSeekBarMode = SEEKMODE.LONGPRESS;
					if(mSpeed == SPEED.X2 && mSeekBarType == SEEKTYPE.BACKWARD)return true;
					mSpeed = SPEED.X2;
					mSeekBarType = SEEKTYPE.BACKWARD;
					mHandler.removeCallbacks(QuickAdjustSeekBar);
					mHandler.postDelayed(QuickAdjustSeekBar, 50);
					return true;
				case KeyEvent.KEYCODE_DPAD_RIGHT:
					mSeekBarMode = SEEKMODE.LONGPRESS;
					if(mSpeed == SPEED.X2 && mSeekBarType == SEEKTYPE.FORWARD)return true;
					mSpeed = SPEED.X2;
					mSeekBarType = SEEKTYPE.FORWARD;
					mHandler.removeCallbacks(QuickAdjustSeekBar);
					mHandler.postDelayed(QuickAdjustSeekBar, 50);
					return true;
				}
			}
			return false;
		}
		public boolean JoyplusonKeyDown(int keyCode, KeyEvent event) {
			// TODO Auto-generated method stub
			Log.d("KeyCode","Bar JoyplusonKeyDown keyCode="+keyCode);
			if(Layout_Time.getVisibility() == View.VISIBLE && (mActivity.getPlayer()!=null)){
				Log.d("KeyCode","Bar JoyplusonKeyDown ");
				switch(keyCode){
				case KeyEvent.KEYCODE_DPAD_LEFT:
					if(mSeekBarMode == SEEKMODE.LONGPRESS){
						JoyplusonKeyDown(KeyEvent.KEYCODE_DPAD_CENTER,null);
						return true;
					}else{
						if(mSeekBarType == SEEKTYPE.FORWARD){
							if(mSpeed == SPEED.X1)mSeekBarType = SEEKTYPE.BACKWARD;
							mSpeed = SPEED.X1;
						}else if(mSpeed == SPEED.X3)return true;
						else {mSpeed = getNextSpeed(mSpeed);
							mSeekBarType = SEEKTYPE.BACKWARD;}
					}
					mHandler.removeCallbacks(QuickAdjustSeekBar);
					mHandler.postDelayed(QuickAdjustSeekBar, 50);
					return true;
				case KeyEvent.KEYCODE_DPAD_RIGHT:
					if(mSeekBarMode == SEEKMODE.LONGPRESS){
						JoyplusonKeyDown(KeyEvent.KEYCODE_DPAD_CENTER,null);
						return true;
					}else{
						if(mSeekBarType == SEEKTYPE.BACKWARD){
							if(mSpeed == SPEED.X1)mSeekBarType = SEEKTYPE.FORWARD;
							mSpeed = SPEED.X1;
						}else if(mSpeed == SPEED.X3)return true;
						else {mSpeed = getNextSpeed(mSpeed);
						      mSeekBarType = SEEKTYPE.FORWARD;}
					}
					mHandler.removeCallbacks(QuickAdjustSeekBar);
					mHandler.postDelayed(QuickAdjustSeekBar, 50);
					return true;
				case KeyEvent.KEYCODE_DPAD_CENTER:
				case KeyEvent.KEYCODE_ENTER:
					if(mSeekBarType != SEEKTYPE.NORMAL){
						mHandler.removeCallbacksAndMessages(null);											
						mHandler.postDelayed(new Runnable(){
							@Override
							public void run() {
								// TODO Auto-generated method stub
								InitSpeed();
							}							
						}, Integer.parseInt(mActivity.getString(R.string.defaultUpdateTime)));
						mActivity.getPlayer().SeekVideo(SeekBar.getProgress());
						return true;
					}
					return false;
				case KeyEvent.KEYCODE_BACK:
				case 111://the keycode was be change to 111 ,but don't know where change
					if(mSeekBarType != SEEKTYPE.NORMAL){
						mSeekBarType = SEEKTYPE.NORMAL;
						mSpeed       = SPEED.X0;
						//mHandler.removeCallbacks(QuickAdjustSeekBar);
						mHandler.removeCallbacksAndMessages(null);
					}else{
					    //mHandler.sendEmptyMessage(MSG_REQUESTHIDE);
						mHandler.removeCallbacksAndMessages(null);
						mHandler.sendEmptyMessage(MSG_REQUESTHIDE);
					}
					return true;
				}
			} 
			return false;
		} 
		public VideoViewController(){
			InitResource();
		}
		public void setVisible(boolean Visiblility){
			if(Debug)Log.d(TAG,"setVisibility("+Visiblility+")");
			Layout_Time.setVisibility(Visiblility?View.VISIBLE:View.GONE);
			Layout_seek.setVisibility(Visiblility?View.VISIBLE:View.GONE);
			if(Visiblility && mSeekBarType == SEEKTYPE.NORMAL)
				UpdateProgress(JoyplusMediaPlayerVideoView.CurrentMediaInfo);
			else{
				Layout_Time.startAnimation(JoyplusMediaPlayerActivity.mAlphaDispear);
				Layout_seek.startAnimation(JoyplusMediaPlayerActivity.mAlphaDispear);
			}
		}
		private void InitResource(){
			if(Debug)Log.d(TAG,"VideoViewController InitResource()");
			Layout_Time       = (RelativeLayout)mActivity.findViewById(R.id.mediacontroller_bottombar);
			CurrentTimeView   = (TextView)      mActivity.findViewById(R.id.mediacontroller_bottombar_current_time);
			TotalTimeView     = (TextView)      mActivity.findViewById(R.id.mediacontroller_bottombar_total_time);
			SeekBar           = (SeekBar)       mActivity.findViewById(R.id.mediacontroller_bottombar_seekbar);
			Layout_seek       = (RelativeLayout)mActivity.findViewById(R.id.mediacontroller_bottombar_seek);
			Layout_Speed      = (RelativeLayout)mActivity.findViewById(R.id.mediacontroller_bottombar_time_fast);
			SpeedView         = (TextView)      mActivity.findViewById(R.id.mediacontroller_bottombar_time_fasttext);
			SeekBar.setEnabled(false);			
		}
		public void UpdateProgress(MediaInfo info){
			//if(Debug)Log.d(TAG,"UpdateProgress()");
			if(mSeekBarType == SEEKTYPE.BACKWARD || mSeekBarType == SEEKTYPE.FORWARD){
				Layout_Speed.setVisibility(View.VISIBLE);
				if(mSeekBarType == SEEKTYPE.FORWARD)
					Layout_Speed.setBackgroundResource(R.drawable.play_time_right);
				else if(mSeekBarType == SEEKTYPE.BACKWARD)
					Layout_Speed.setBackgroundResource(R.drawable.play_time_left);
				CurrentTimeView.setText(getTimeString(SeekBar.getProgress()));
				SpeedView.setText(mSpeed.toString());
				updateSeekBar(null);
				return;
			}
			if(mSeekBarType != SEEKTYPE.NORMAL)return;
			Layout_Speed.setVisibility(View.GONE);
			if(info != null){Log.d(TAG,"UpdateProgress info="+info.toString());
				//if(info.getState().toInt()>=STATE.MEDIA_STATE_INITED.toInt()){
			    if(//info.getState() == STATE.MEDIA_STATE_PUSE||
			    		info.getState() == STATE.MEDIA_STATE_PLAYING
						||info.getState() == STATE.MEDIA_STATE_INITED){
					CurrentTimeView.setText(getTimeString((int)info.getCurrentTime()));
					TotalTimeView.setText(getTimeString((int)info.getTotleTime()));
					updateSeekBar(info);
				}
			}else{
				CurrentTimeView.setText(getTimeString(0));
				TotalTimeView.setText(getTimeString(0));
				SeekBar.setMax(100);
				SeekBar.setProgress(0);
				updateSeekBar(null);
			}
		}
		private void updateSeekBar(MediaInfo info){		
			if (info != null && (info.getState().toInt()>=STATE.MEDIA_STATE_INITED.toInt()) && mSeekBarType == SEEKTYPE.NORMAL){
				SeekBar.setMax((int) info.getTotleTime());
				SeekBar.setProgress((int) info.getCurrentTime());
			}else if(info == null && mSeekBarType == SEEKTYPE.NORMAL){
				SeekBar.setMax(100);
				SeekBar.setProgress(0);
			}
			UpdateSeekTime();
		}
		private void UpdateSeekTime(){
			RelativeLayout.LayoutParams parms = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.WRAP_CONTENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT);
			parms.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);	
			if (SeekBar.getProgress()>0){
				double mLeft = (double) SeekBar.getProgress() / SeekBar.getMax()* 
						(SeekBar.getMeasuredWidth() - UtilTools.getStandardValue(mActivity, seekBarWidthOffset)) + OFFSET;
				parms.leftMargin = (int) mLeft;
			}else{
				parms.leftMargin = UtilTools.getStandardValue(mActivity, OFFSET);
			}
			parms.bottomMargin = UtilTools.getStandardValue(mActivity,(20 + 10));
			Layout_Time.setLayoutParams(parms);
		}
		private String getTimeString(int time){
			if(time<0)time = 0;
			StringBuffer sb = new StringBuffer();
			time/=1000;
	        sb.append(getString(time/(60*60)));
	        sb.append(":");
	        time%=(60*60);
	        sb.append(getString(time/60));
	        sb.append(":");
	        time%=60;
	        sb.append(getString(time));
	        return sb.toString();
		}
		private String getString(int time){
			StringBuffer sb = new StringBuffer();
			sb.append(time/10).append(time%10);
			return sb.toString();
		}
	}

	@Override
	public boolean JoyplusdispatchMessage(Message msg) {
		// TODO Auto-generated method stub
		mHandler.dispatchMessage(msg);
		return false;
	}
	@Override
	public boolean JoyplusonKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(mBottomBar.Layout_Time.getVisibility()== View.VISIBLE){
			if(mBottomBar.JoyplusonKeyDown(keyCode, event)){
				mHandler.sendEmptyMessage(MSG_REQUESTSHOW);
				return true;
			}
			//return true;
		}
		return false;
	}

	@Override
	public void JoyplussetVisible(boolean visible,int layout) {
		// TODO Auto-generated method stub
		if(Debug)Log.d(TAG,"setVisible("+visible+")");
		setVisible(visible,0);
	}
	@Override
	public int JoyplusgetLayout() {
		// TODO Auto-generated method stub
		return LAYOUT_BAR;
	}
	@Override
	public boolean JoyplusonKeyLongPress(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(mBottomBar.Layout_Time.getVisibility()== View.VISIBLE){
			if(mBottomBar.JoyplusonKeyLongPress(keyCode, event)){
				//mHandler.sendEmptyMessage(MSG_REQUESTSHOW);
				return true;
			}

		}
		return false;
	}
}