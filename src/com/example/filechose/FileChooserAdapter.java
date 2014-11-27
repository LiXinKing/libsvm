package com.example.filechose;

import java.io.File;
import java.util.ArrayList;

import com.example.libsvm.R;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class FileChooserAdapter extends BaseAdapter {

	private ArrayList<FileInfo> mFileLists;
	private LayoutInflater mLayoutInflater = null;

//	private static ArrayList<String> PPT_SUFFIX = new ArrayList<String>();
	private static ArrayList<String> SVM_SUFFIX = new ArrayList<String>();

//	static {
//		PPT_SUFFIX.add(".ppt");
//		PPT_SUFFIX.add(".pptx");
//	}
	static {
		SVM_SUFFIX.add(".txt");//�����ó�ֻ֧��txt
//		SVM_SUFFIX.add(".svm");
	}

	public FileChooserAdapter(Context context, ArrayList<FileInfo> fileLists) {
		super();
		mFileLists = fileLists;
		mLayoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mFileLists.size();
	}

	@Override
	public FileInfo getItem(int position) {
		// TODO Auto-generated method stub
		return mFileLists.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View view = null;
		ViewHolder holder = null;
		if (convertView == null || convertView.getTag() == null) {
			view = mLayoutInflater.inflate(R.layout.filechooser_gridview_item,
					null);
			holder = new ViewHolder(view);
			view.setTag(holder);
		} else {
			view = convertView;
			holder = (ViewHolder) convertView.getTag();
		}

		FileInfo fileInfo = getItem(position);
        //TODO 
		
		holder.tvFileName.setText(fileInfo.getFileName());
		
		if(fileInfo.isDirectory()){      //�ļ���
			holder.imgFileIcon.setImageResource(R.drawable.ic_folder);
			holder.tvFileName.setTextColor(Color.GRAY);
		}

		else if(fileInfo.issvmfile()){
			holder.imgFileIcon.setImageResource(R.drawable.ic_svm);
			holder.tvFileName.setTextColor(Color.BLUE);
		}
		else {                           //δ֪�ļ�
//			holder.imgFileIcon.setImageResource(R.drawable.ic_file_unknown);
//			holder.tvFileName.setTextColor(Color.GRAY);
			//�����ӣ�����Ҫ���޸�filechooseactivity�ĵ�88��
		}
		return view;
	}

	static class ViewHolder {
		ImageView imgFileIcon;
		TextView tvFileName;

		public ViewHolder(View view) {
			imgFileIcon = (ImageView) view.findViewById(R.id.imgFileIcon);
			tvFileName = (TextView) view.findViewById(R.id.tvFileName);
		}
	}

	
	enum FileType {
		FILE , DIRECTORY;
	}

	// =========================
	// Model
	// =========================
	static class FileInfo {
		private FileType fileType;
		private String fileName;
		private String filePath;

		public FileInfo(String filePath, String fileName, boolean isDirectory) {
			this.filePath = filePath;
			this.fileName = fileName;
			fileType = isDirectory ? FileType.DIRECTORY : FileType.FILE;
		}


		public boolean issvmfile(){
			if(fileName.lastIndexOf(".") < 0)  //Don't have the suffix 
				return false ;
			String fileSuffix = fileName.substring(fileName.lastIndexOf("."));
			if(!isDirectory() && SVM_SUFFIX.contains(fileSuffix))
				return true ;
			else
				return false ;
		}
		public boolean isDirectory(){
			if(fileType == FileType.DIRECTORY)
				return true ;
			else
				return false ;
		}
		
		public String getFileName() {
			return fileName;
		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}

		public String getFilePath() {
			return filePath;
		}

		public void setFilePath(String filePath) {
			this.filePath = filePath;
		}

		@Override
		public String toString() {
			return "FileInfo [fileType=" + fileType + ", fileName=" + fileName
					+ ", filePath=" + filePath + "]";
		}
	}

}
