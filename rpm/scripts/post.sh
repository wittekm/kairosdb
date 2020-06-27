set -e
/sbin/chkconfig --add kairosdb
mkdir -p /opt/kairosdb/logs
chown kairosdb:kairosdb /opt/mad/logs
mkdir -p /opt/kairosdb/config
mkdir -p /var/run/kairosdb
chown kairosdb:kairosdb /var/run/kairosdb
