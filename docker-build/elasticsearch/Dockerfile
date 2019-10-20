# 基于es官方提供的镜像构建
FROM docker.elastic.co/elasticsearch/elasticsearch:6.8.3

# ik插件安装
RUN /usr/share/elasticsearch/bin/elasticsearch-plugin install --batch http://192.168.1.14/soft/elasticsearch-analysis-ik-6.8.3.zip
# 添加ik配置文件
ADD ./IKAnalyzer.cfg.xml /usr/share/elasticsearch/config/analysis-ik/IKAnalyzer.cfg.xml
