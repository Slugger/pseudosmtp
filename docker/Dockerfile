#
# Copyright 2016 Battams, Derek
# 
#	Licensed under the Apache License, Version 2.0 (the "License");
#	you may not use this file except in compliance with the License.
#	You may obtain a copy of the License at
# 
#		http://www.apache.org/licenses/LICENSE-2.0
#
#	Unless required by applicable law or agreed to in writing, software
#	distributed under the License is distributed on an "AS IS" BASIS,
#	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#	See the License for the specific language governing permissions and
#	limitations under the License.
#
FROM jetty:9-jre7
MAINTAINER derek@battams.ca
COPY war/*.war /var/lib/jetty/webapps/
COPY conf/etc/* /etc/
COPY conf/default/* /etc/default/
COPY scripts/* /usr/local/jetty/bin/
RUN chmod 755 /usr/local/jetty/bin/*
RUN chown -R jetty.jetty /var/lib/jetty/webapps
EXPOSE 8080 8000
CMD /usr/local/jetty/bin/jetty.sh run
