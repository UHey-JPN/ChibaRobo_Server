# ChibaRobo_Server

## システム概要
千葉ロボシステムの中枢を担うプログラムです。Java EE 1.8で動作します。機能としては次のようなものとなります。
* ショーの種類と時間の管理
* トーナメントの進行状況の管理

## 通信用パケットの概要
### サーバから送信するUDPパケット
サーバからポート58239に定期的に送信。パケットの種類は以下の3(4)種類。1行目がパケットの種類を表す。
1. Server State パケット  
	* サーバのIPアドレスやポート番号を記載。
	* console はサーバ操作用のTCPポート番号。
	* database_port はデータベースを取得するためのTCPポート番号。
	* kam_port はKeepAliveパケットを送信するためのUDPポート番号。

2. Show State パケット
	* 現在のショーモードを送信するパケット。
	* 2行目から順にパケットID, 試合番号, ショーモード, ショーモードの開始時間, ゲームの得点, 左側チームのチーム情報, 左側チームのロボット情報×2行, 右側チームのチーム情報, 右側チームのロボット情報×2行, 

3. Tournament State パケット
	* トーナメントの状態を送信
	* 試合番号の順にCSVで状態を送信。

	| 値    | 説明 |
	|:---------|:---------:|
	| -1 |  終了していない。|
	| 0  | 左チームが勝利。|
	| 1  |  右チームが勝利。|

### サーバ以外から送信するUDPパケット
1. KeepAliveパケットのみ。
	* ShowStateパケットを受信するたびにUDPで送信する。
	* サーバにあるKeepAliveManagerが名前を管理できるようになっているので、各クライアントは識別できるような名前をKeepAliveパケットに載せるべき。
	* 名前は適当でも問題ないし、かぶっても大丈夫だが、ユーザーに優しくない。

### TCPでサーバーから取得するデータベース  
次の3種類。指定方法はクライアントからデータの種類と文字コードを最初に送信。文字コードはutf-8かShitf_JIS。
1. Robot List  
　クライアントから"robot,utf-8"または"robot,Shift_JIS"と送信。
　"ACK"が返ってきたあとに、Robot ListがXMLで返ってくる。

2. Team List  
　クライアントから"team,utf-8"または"team,Shift_JIS"と送信。
　"ACK"が返ってきたあとに、Team ListがXMLで返ってくる。

3. Tournament Data  
　クライアントから"tournament,utf-8"または"tournament,Shift_JIS"と送信。
　"ACK"が返ってきたあとに、Tournament DataがXMLで返ってくる。


----

以下、パケットおよび通信の詳細です。
```txt:Server State パケット
【Server State パケット】
server
192.168.11.4
console,56122
database_port,56121
kam_port,61847
```

```txt:Show State パケット
【Show State パケット】
show
26521
0
game
2016,8,18,1,45,6,305
0,0
2,3,4,Rosenburg Diavolos 
3,SadSpring,瀧上　颯太,B4
4,たったかぴょーん,松浦　修平,B4
3,5,6,トラベリング
5,Magic Hat,松村　悠平,B4
6,かずこ,宮内　愛奈,B4
```

```txt:Tournament State パケット
【Tournament State パケット】
tournament
-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1
```

```txt:KeepAliveパケット
【KeepAliveパケット】
ClientName,Show State パケットのパケット番号
```

```xml:Robot List
【Robot List】
ACK
<?xml version="1.0" encoding="utf-8" ?>
<roboList>
  <robot id="1">
    <name>ゲートボーラーGGI</name>
    <creater>末石　悟</creater>
    <grade>B4</grade>
    <img>1_末石2.JPG</img>
    <desc>1号だよ</desc>
  </robot>
  <robot id="2">
    <name>ごった煮・カオス・一郎</name>
    <creater>石井　智之</creater>
    <grade>B4</grade>
    <img>15_石井.jpg</img>
    <desc>2号だよ</desc>
  </robot>
  <robot id="3">
    <name>SadSpring</name>
    <creater>瀧上　颯太</creater>
    <grade>B4</grade>
    <img>2_瀧上.jpg</img>
    <desc>3号だよ</desc>
  </robot>
=============================================
    中略
=============================================
  <robot id="45">
    <name>駄D夢ドリームV</name>
    <creater>安藤</creater>
    <grade>M2</grade>
    <img>安藤.jpg</img>
    <desc>45号だよ</desc>
  </robot>
  <robot id="99">
    <name>　</name>
    <creater>DummyImg</creater>
    <grade>B0</grade>
    <img>Dummy.jpg</img>
    <desc>Dummy用</desc>
  </robot>
  <robot id="100">
    <name>　</name>
    <creater>DummyImg</creater>
    <grade>B0</grade>
    <img>Dummy.jpg</img>
    <desc>Dummy用</desc>
  </robot>
</roboList>
```

```xml:Team List
【Team List】
ACK
<?xml version="1.0" encoding="utf-8" ?>
<teamList>
  <team id="1">
    <name>イシマル電機</name>
    <robot id="1" />
    <robot id="2" />
    <desc>チーム1です</desc>
  </team>
  <team id="2">
    <name>Rosenburg Diavolos </name>
    <robot id="3" />
    <robot id="4" />
    <desc>チーム2です</desc>
  </team>
  <team id="3">
    <name>トラベリング</name>
    <robot id="5" />
    <robot id="6" />
    <desc>チーム3です</desc>
  </team>
=============================================
    中略
=============================================
  <team id="22">
    <name>黒糖もんじゃアイス</name>
    <robot id="24" />
    <robot id="35" />
    <desc>チーム22です</desc>
  </team>
  <team id="23">
    <name>エクスカリバー</name>
    <robot id="39" />
    <robot id="40" />
    <desc>チーム23です</desc>
  </team>
  <team id="100">
    <name>　</name>
    <robot id="99" />
    <robot id="100" />
    <desc>Dummy用</desc>
  </team>
</teamList>
```

```xml:Tournament Data
【Tournament Data】
ACK
<?xml version="1.0" encoding="1.0" ?>
<tournament>
  <game id="21">
    <game id="19">
      <game id="15">
        <game id="7">
          <team id="0" />
          <game id="0">
            <team id="1" />
            <team id="2" />
          </game>
        </game>
        <game id="8">
          <team id="3" />
          <team id="4" />
        </game>
      </game>
      <game id="16">
        <game id="9">
          <team id="5" />
          <game id="1">
            <team id="6" />
            <team id="7" />
          </game>
        </game>
        <game id="10">
          <game id="2">
            <team id="8" />
            <team id="9" />
          </game>
          <team id="10" />
        </game>
      </game>
    </game>
    <game id="20">
      <game id="17">
        <game id="11">
          <team id="11" />
          <game id="3">
            <team id="12" />
            <team id="13" />
          </game>
        </game>
        <game id="12">
          <game id="4">
            <team id="14" />
            <team id="15" />
          </game>
          <team id="16" />
        </game>
      </game>
      <game id="18">
        <game id="13">
          <team id="17" />
          <game id="5">
            <team id="18" />
            <team id="19" />
          </game>
        </game>
        <game id="14">
          <game id="6">
            <team id="20" />
            <team id="21" />
          </game>
          <team id="22" />
        </game>
      </game>
    </game>
  </game>
</tournament>
```
