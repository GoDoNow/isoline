package isoline;

//import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class Isoline {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		float[][] mat = { { 0, 1, 2, 3, 5, 7, 13, 18 }, { 1, 2, 3, 5, 7, 13, 18, 15 }, { 2, 3, 6, 7, 27, 17, 14, 11 },
				{ 5, 9, 11, 28, 34, 28, 15, 10 }, { 14, 17, 27, 35, 28, 15, 11, 7 }, { 17, 26, 33, 33, 26, 14, 9, 6 },
				{ 25, 30, 28, 25, 14, 11, 6, 4 }, { 27, 26, 26, 14, 10, 9, 1, 0 } };
		// 单个场等值线测试
		List<Object> list = isolineByValue(mat, 1, 16);
		for (int i = 0; i < list.size() - 1; i++) {
			Point2D[] point2ds = (Point2D[]) list.get(i);
			System.out.println("------一条等值线--------");
			for (int j = 0; j < point2ds.length; j++) {
				Point2D point2d = point2ds[j];
				System.out.print("(" + point2d.getX() + "," + point2d.getY() + ")");
			}
			System.out.println();
		}
		// 场等值线测试
		List<Object> dataList = isoline(mat, 1, -10);
		// 最后一个数组为等值线值
		List<Float> valueArray = (List<Float>) dataList.get(dataList.size() - 1);
		for (int i = 0; i < dataList.size() - 1; i++) {
			List<Point2D[]> l = (List<Point2D[]>) dataList.get(i);
			System.out.println("------等值线值--------" + valueArray.get(i));
			for (Point2D[] point2ds : l) {
				System.out.println("------一条等值线--------");
				for (int j = 0; j < point2ds.length; j++) {
					Point2D point2d = point2ds[j];
					System.out.print("(" + point2d.getX() + "," + point2d.getY() + ")");
				}
				System.out.println();
			}
		}
	}

	// ###########-----------------------测试方法-------------------start
	// 单个等值线测试
	public static List<Object> isolineByValue(float[][] mat, float step, float value) {
		Isoline iso = new Isoline(step, mat, true);
		List<Object> grpIsoline = iso.ScanIsoline(value);
		return grpIsoline;
	}

	// 场等值线测试
	public static List<Object> isoline(float[][] mat, float step, float minVal) {
		Isoline iso = new Isoline(step, mat, false);
		List<Object> fieldIsoline = iso.ScanField(minVal);
		return fieldIsoline;
	}
	// ###########-----------------------测试方法-------------------end

	/**
	 * 产生单值等值线
	 */
	private List<Object> ScanIsoline(float zc) {
		TreeMap<Integer, List<IdxPoint2D>> nlist = SetNodeList(zc);
		List<Object> grpIsoline = new ArrayList<Object>();// 相同值的一组等值线

		while ((nlist != null) && (nlist.size() > 0)) {
			PnoCurno pnocurno = FindCurno(nlist);
			int firstno = pnocurno.curno;
			// SortedList sublist = (SortedList)nlist[curno]; //单元子表
			ArrayList<Point2D> isoline = new ArrayList<Point2D>();// 一根等值线
			do {
				int i = AddPointF(nlist, zc, pnocurno.pno, pnocurno.curno, isoline); // 添加等值点

				pnocurno.pno = pnocurno.curno;
				pnocurno.curno = i;
			} while ((nlist.size() > 0) && (nlist.get(pnocurno.curno) != null) && (pnocurno.curno != firstno)
					&& (pnocurno.curno >= 0));
			if (isoline.size() >= 4) {
				Point2D[] pf = ListToPoints(isoline);// 将Array转换为PointF[]
				// pf = GraphTool.BSplinePointF(pf); //将折线点转换为BSpline点
				if (pf.length > 0) {
					grpIsoline.add(pf);
				}
			}
		}
		return grpIsoline;
	}

	/**
	 * 产生场等值线
	 * 
	 * @param 最小开始计算的等值线值
	 * @return
	 */
	private List<Object> ScanField(float minVal) {
		MinMax mm = FindMinMaxVal();
		List<Object> fldIsoline = new ArrayList<Object>();
		List<Object> isoline = new ArrayList<Object>();
		float zc = mm.minVal;
		System.out.println("等值线最低最高值： " + mm.minVal + " " + mm.maxVal);
		while (zc <= mm.maxVal) {
			if (zc >= minVal) {
				List<Object> list = ScanIsoline(zc);
				if (list.size() > 0) {
					isoline.add(zc);
					fldIsoline.add(list);
				}

			}
			zc += step;
		}
		fldIsoline.add(isoline);
		return fldIsoline;
	}

	@SuppressWarnings("unchecked")
	private List<IdxPoint2D> putToSort(List<IdxPoint2D> l, Integer key, Point2D xy) {
		IdxPoint2D ip = new IdxPoint2D(key, xy);
		l.add(ip);
		Collections.sort(l, new Comparator() {
			public int compare(Object o1, Object o2) {
				IdxPoint2D e1 = (IdxPoint2D) o1;
				IdxPoint2D e2 = (IdxPoint2D) o2;
				int c = e1.idx.compareTo(e2.idx);
				return c;
			}
		});
		return l;
	}

	private IdxPoint2D getFromSort(List<IdxPoint2D> sortedList, Integer key) {
		int p = sortedList.size() / 2;
		IdxPoint2D ip = sortedList.get(p);
		for (int i = 0; i < sortedList.size(); i++) {
			int c = ip.idx.compareTo(key);
			switch (c) {
			case -1:
				p = (p + sortedList.size()) / 2;
				break;
			case 0:
				return ip;
			case 1:
				p /= 2;
				break;
			}
			ip = sortedList.get(p);
		}
		return null;
	}

	private class MinMax {
		private float minVal = 1e9f, maxVal = -1e9f;

		public MinMax(float mn, float mx) {
			minVal = mn;
			maxVal = mx;
		}
	}

	private float[][] mat;// 原始二维矩阵。
	private Point2D[][] matxy;
	private float step;// 当前分析值，步长。
	private int rows, cols;

	/**
	 * 初始化和数据处理
	 * 
	 * @param step
	 *            步长
	 * @param mat
	 *            数据数组
	 * @param isneedYChange
	 *            是否需要转换y轴数据顺序
	 */
	private Isoline(float step, float[][] mat, boolean isneedYChange) {
		this.step = step;
		this.rows = mat.length;
		this.cols = mat[0].length;
		this.mat = mat;
		if (isneedYChange) {
			this.mat = YChange(mat);
		}
		matxy = new Point2D[rows][cols];
		SetDeaultMatxy();
	}

	// 设定mat缺省的xy坐标（与迪卡尔方向一致）
	private void SetDeaultMatxy() {
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				matxy[i][j] = new Point2D(j, i);
			}
		}
	}

	// 设定mat的xy坐标（与迪卡尔方向一致）
	private void SetMatxy(Point2D[][] mxy) {
		for (int i = 0; i < rows; i++) {
			int k = rows - i - 1;
			for (int j = 0; j < cols; j++) {
				this.matxy[k][j] = new Point2D(mxy[k][j].getX(), mxy[k][j].getY());
			}
		}
	}

	// 改变行方向序与迪卡尔方向一致
	private float[][] YChange(float[][] mat) {
		float[][] m = new float[rows][cols];
		for (int i = 0; i < rows; i++) {
			int k = rows - i - 1;
			for (int j = 0; j < cols; j++) {
				m[k][j] = mat[i][j];
			}
		}
		return m;
	}

	/**
	 * 对分析场找到起始、终止点、去掉角点等值点
	 * 
	 * @return
	 */
	private MinMax FindMinMaxVal() {
		float a;
		MinMax mm = new MinMax(INF, -INF);
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				a = mat[i][j];
				if (a % step == 0) {
					// 避免等值点出现在角点
					a += 1e-5f;
					mat[i][j] = a;
				}
				mm.minVal = Math.min(a, mm.minVal);
				mm.maxVal = Math.max(a, mm.maxVal);
			}
		}
		a = (int) (mm.minVal / step) * step;
		if (a < mm.minVal) {
			mm.minVal = a + step;
		} else {
			mm.minVal = a;
		}
		mm.maxVal = (int) (mm.maxVal / step) * step;
		return mm;
	}

	/**
	 * 计算等值点坐标
	 */
	private Point2D CalcuPointF(float zc, int xi, int yi, float zi, int xj, int yj, float zj) {
		Point2D pc = new Point2D(0, 0);
		if ((zj - zi) != 0) {
			float z = (zc - zi) / (zj - zi);
			Point2D di = matxy[yi][xi], dj = matxy[yj][xj];
			float x = di.getX() + z * (dj.getX() - di.getX());
			float y = di.getY() + z * (dj.getY() - di.getY());
			pc = new Point2D(x, y);
		}
		return pc;
	}

	/**
	 * 计算当前单元curuno的相邻单元； Neighbor<0,表示等值点坐标在边界上；
	 */
	private int Neighbor(int curno, int i0, int j0, int i1, int j1) {
		int uno, c1 = cols - 1, n = (rows - 1) * c1;// 相邻单元号

		CordIJ cordij = CalcuCord(curno);
		if (i0 == i1) {

			if (i0 == cordij.i) {
				uno = curno - c1;// 相邻单元在上一行
			} else {
				uno = curno + c1; // 相邻单元在下一行
				if (uno > n) {
					uno = -uno; // 等值点坐标在边界上

				}

			}
		} else if (j0 == j1) {
			if (j0 == cordij.j) {
				uno = curno - 1; // 相邻单元在左列
				if ((cordij.j == 0) && (uno > 0)) {
					uno = -uno;
				}
			} else {
				uno = curno + 1; // 相邻单元在右列
				if ((uno >= c1) && (uno % c1 == 0)) {
					uno = -uno;// 等值点坐标在边界上
				}

			}
		} else {
			System.out.println("不能计算相邻单元i0!==j1 && j0 != j1");
			uno = 0;
		}
		return uno;
	}

	/**
	 * 判断当前单元curuno是否有等值点，若有添加之
	 */
	private float ClcuF(float zc, int i0, int j0, int i1, int j1) {
		float zi = mat[i0][j0], zj = mat[i1][j1], ff = 0.0f;
		if ((zj - zc) != 0.0f) {
			ff = (zi - zc) / (zj - zc);
		}
		return ff;
	}

	Integer INF = 10000000;

	private List<IdxPoint2D> put(List<IdxPoint2D> sortedlist, Integer key, Point2D xy) {
		sortedlist = putToSort(sortedlist, key, xy);
		return sortedlist;
	}

	private Point2D get(List<IdxPoint2D> sortedlist, Integer key) {
		Point2D xy;
		xy = getFromSort(sortedlist, key).xy;
		return xy;
	}

	private TreeMap<Integer, List<IdxPoint2D>> put(TreeMap<Integer, List<IdxPoint2D>> sortedlist, Integer key,
			List<IdxPoint2D> slist) {
		sortedlist.put(key, slist);
		return sortedlist;
	}

	private List<IdxPoint2D> get(TreeMap<Integer, List<IdxPoint2D>> sortedlist, Integer key) {
		List<IdxPoint2D> slist;
		slist = sortedlist.get(key);
		return slist;
	}

	/**
	 * 判断当前单元curuno是否有等值点，若有添加之
	 */
	private List<IdxPoint2D> f(float zc, int curno, List<IdxPoint2D> sublist, int i0, int j0, int i1, int j1) {
		if (ClcuF(zc, i0, j0, i1, j1) < 0) {
			if (sublist == null) {
				System.out.println("sublist 不应该为空");
				return null;
			}
			Point2D pc = CalcuPointF(zc, j0, i0, mat[i0][j0], j1, i1, mat[i1][j1]);// 计算等值点坐标

			int neighber = Neighbor(curno, i0, j0, i1, j1); // 计算邻居

			sublist = put(sublist, neighber, pc);
		}
		return sublist;
	}

	/**
	 * 判断当前单元curuno是否有等值点，若有添加之
	 */
	private TreeMap<Integer, List<IdxPoint2D>> Setneighber(TreeMap<Integer, List<IdxPoint2D>> nlist, float zc,
			int curno, int i, int j) {
		List<IdxPoint2D> sublist = new ArrayList<IdxPoint2D>();
		sublist = f(zc, curno, sublist, i, j, i, j + 1);
		sublist = f(zc, curno, sublist, i, j, i + 1, j);
		sublist = f(zc, curno, sublist, i + 1, j, i + 1, j + 1);
		sublist = f(zc, curno, sublist, i, j + 1, i + 1, j + 1);
		if (sublist.size() >= 2) {
			nlist = put(nlist, curno, sublist);
		}
		return nlist;
	}

	/**
	 * 建立等值线zc的等值点链表
	 */
	private TreeMap<Integer, List<IdxPoint2D>> SetNodeList(float zc) {
		TreeMap<Integer, List<IdxPoint2D>> nlist = new TreeMap<Integer, List<IdxPoint2D>>(); // nlist清空
		int k = 0;
		for (int i = 0; i < rows - 1; i++) {
			for (int j = 0; j < cols - 1; j++) {
				nlist = Setneighber(nlist, zc, k++, i, j);
			}
		}
		return nlist;
	}

	/**
	 * 根据单元号，返回左上角坐标
	 */
	private CordIJ CalcuCord(int uno) {
		CordIJ cordij = new CordIJ(-1, -1);
		if (uno >= 0) {
			cordij.i = uno / (cols - 1);
			cordij.j = uno % (cols - 1);
		}
		return cordij;
	}

	/**
	 * 返回链表中第一个关键字
	 */
	private static Integer Key0(List<IdxPoint2D> list) {
		return list.get(0).idx;
	}

	/**
	 * 添加曲线点
	 */
	private int Mathch2(int pno, int curno, ArrayList<Point2D> isoline, List<IdxPoint2D> sublist) {
		int k;
		Point2D p;
		if (isoline.size() == 0) { // 初始情况，添加两个等值点
			isoline.add(sublist.get(0).xy);
			p = sublist.get(1).xy;
			k = sublist.get(1).idx;
		} else if (sublist.get(0).idx == pno) {
			p = sublist.get(1).xy;
			k = sublist.get(1).idx;
		} else if (sublist.get(1).idx == pno) {
			p = sublist.get(0).xy;
			k = sublist.get(0).idx;
		} else {
			k = -1;
			sublist.clear();
			return k;
		}
		isoline.add(p);
		sublist.clear();
		return k;
	}

	/**
	 * 查找鞍点出口
	 */
	private int Saddle(float zc, int pno, int curno) {
		int pi = pno;
		CordIJ cordij = CalcuCord(curno);// 单元格左上角
		int i = cordij.i, j = cordij.j;
		float zI = mat[i + 1][j], zJ = mat[i + 1][j + 1], zK = mat[i][j + 1], zL = mat[i][j],
				zP = ((zI + zJ + zK + zL) / 4.0f) - zc;
		zI = (zI - zc) * zP;
		zJ = (zJ - zc) * zP;
		zK = (zK - zc) * zP;
		zL = (zL - zc) * zP;

		if (Neighbor(curno, i, j, i, j + 1) == pno) { // 上
			if (zL <= 0) {
				pi = Neighbor(curno, i, j, i + 1, j);
			} else {
				pi = Neighbor(curno, i, j + 1, i + 1, j + 1);
			}
		} else if (Neighbor(curno, i + 1, j, i + 1, j + 1) == pno) { // 下
			if (zI <= 0) {
				pi = Neighbor(curno, i, j, i + 1, j);
			} else {
				pi = Neighbor(curno, i, j + 1, i + 1, j + 1);
			}
		} else if (Neighbor(curno, i, j, i + 1, j) == pno) { // 左
			if (zL <= 0) {
				pi = Neighbor(curno, i, j, i, j + 1);
			} else {
				pi = Neighbor(curno, i + 1, j, i + 1, j + 1);
			}
		} else if (Neighbor(curno, i, j + 1, i + 1, j + 1) == pno) { // 右
			if (zK <= 0) {
				pi = Neighbor(curno, i, j, cordij.i, j + 1);
			} else {
				pi = Neighbor(curno, i + 1, j, i + 1, j + 1);
			}
		}
		return pi;
	}

	/**
	 * 处理鞍点,返回曲线点
	 */
	private int Mathch4(float zc, int pno, int curno, ArrayList<Point2D> isoline, List<IdxPoint2D> sublist) {
		Integer k = new Integer(-1);
		List<IdxPoint2D> sb2 = new ArrayList<IdxPoint2D>();
		IdxPoint2D ip = getFromSort(sublist, pno);
		Point2D p = ip.xy;
		sb2 = put(sb2, pno, p);
		sublist.remove(ip);
		k = Saddle(zc, pno, curno);
		ip = getFromSort(sublist, k);
		p = ip.xy;
		sb2 = put(sb2, k, p);
		sublist.remove(ip);
		return Mathch2(pno, curno, isoline, sb2);
	}

	/**
	 * 添加等值点到等值线
	 */
	private int AddPointF(TreeMap<Integer, List<IdxPoint2D>> nlist, float zc, int pno, int curno,
			ArrayList<Point2D> isoline) {
		int neighber = 0;
		List<IdxPoint2D> sublist = nlist.get(curno); // 单元子表
		if (sublist.size() == 2) {
			neighber = Mathch2(pno, curno, isoline, sublist);
		} else if (sublist.size() == 4) {
			neighber = Mathch4(zc, pno, curno, isoline, sublist);
		}
		if (sublist.size() <= 0) {
			nlist.remove(curno);// 如果非鞍点，删除之
		}
		return neighber;
	}

	private static Point2D[] ListToPoints(ArrayList<Point2D> a) {
		Point2D[] p = new Point2D[a.size()];
		for (int i = 0; i < p.length; i++) {
			p[i] = a.get(i);
		}
		return p;
	}

	/**
	 * 先找边界,在找中间
	 */
	private PnoCurno FindCurno(TreeMap<Integer, List<IdxPoint2D>> nlist) {
		PnoCurno pnocurno = new PnoCurno(0, 0);
		Set mappings = nlist.entrySet();
		for ( Iterator i = mappings.iterator(); i.hasNext();) {
			Map.Entry me = (Map.Entry) i.next();
			pnocurno.curno = (Integer) me.getKey();
			List<IdxPoint2D> sublist = get(nlist, pnocurno.curno);
			pnocurno.pno = sublist.get(0).idx;
			if (pnocurno.pno < 0) {
				return pnocurno;
			}
		}
		return pnocurno;
	}

	// 等值点相邻号
	public class PnoCurno {
		private int pno, curno;

		public PnoCurno(int p, int c) {
			pno = p;
			curno = c;
		}
	}

	public class CordIJ {
		private int i, j;

		public CordIJ(int i0, int j0) {
			i = i0;
			j = j0;
		}
	}

	public class IdxPoint2D {
		private Integer idx;
		private Point2D xy;

		public IdxPoint2D(Integer idx, Point2D p) {
			this.idx = idx;
			xy = p;
		}
	}

	public class Point2D {
		private float x;
		private float y;

		public Point2D(float x, float y) {
			this.x = x;
			this.y = y;
		}

		public float getX() {
			return x;
		}

		public float getY() {
			return y;
		}
	}
}
