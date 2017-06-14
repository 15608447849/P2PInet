package utils;

import client.obj.SerializeSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

/**
 * Created by user on 2017/5/25.
 * 文件查询
 */
public class FindFileVisitor extends SimpleFileVisitor<Path> {
    private ArrayList<File> fileList = new ArrayList<>();
    private Path homeDir;
    private SerializeSource source = null;
    public FindFileVisitor(String homeDir) {
       this.homeDir = Paths.get(homeDir);
    }
    public Path getHomePath(){
        return homeDir;
    }
    public FindFileVisitor setQuerySource(SerializeSource source){
        this.source = source;
        return this;
    }
    public ArrayList<File> ergodicAll(){
        try {
            fileList.clear();
            long time = System.currentTimeMillis();
            Files.walkFileTree(homeDir, this);
            System.out.println("检测资源耗时:" + (System.currentTimeMillis() - time) +" 毫秒.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileList;
    }

    public boolean ergodicOnes(){
            Path fileParh = Paths.get(source.getPosition());
            File file = fileParh.toFile();
            return equalsMD5(file,source.getMd5());
    }
    @Override
    public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) {
            File file = filePath.toFile();
        if ( file.getAbsolutePath().contains(source.getPosition()) || file.length() == source.getSize()) {
            //比较md5
            if (equalsMD5(file,source.getMd5())){
                fileList.add(file);
            }
        }
        return FileVisitResult.CONTINUE;
    }
    //比较md5
    private boolean equalsMD5(File file, String sourceMD5) {
        try {
            String localFileMD5 = MD5Util.getFileMD5String(file);
            if (localFileMD5.equals(sourceMD5)) return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public FileVisitResult visitFileFailed(Path path, IOException e) throws IOException {
        return FileVisitResult.SKIP_SUBTREE;
    }


}
