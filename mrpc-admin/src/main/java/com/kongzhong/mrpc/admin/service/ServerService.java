package com.kongzhong.mrpc.admin.service;

import com.blade.ioc.annotation.Bean;
import com.blade.kit.StringKit;
import com.kongzhong.mrpc.admin.model.RpcServer;
import com.kongzhong.mrpc.admin.model.RpcService;
import com.kongzhong.mrpc.admin.vo.ServerDetailVO;
import com.kongzhong.mrpc.admin.vo.ServerMap;
import com.kongzhong.mrpc.admin.vo.ServerVO;
import com.kongzhong.mrpc.enums.NodeStatusEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.github.biezhi.anima.Anima.delete;
import static io.github.biezhi.anima.Anima.select;
import static io.github.biezhi.anima.Anima.update;

/**
 * @author biezhi
 * @date 2018/6/7
 */
@Bean
public class ServerService {

    public void saveServer(RpcServer rpcServer) {
        RpcServer temp = select().from(RpcServer.class)
                .where(RpcServer::getAppId, rpcServer.getAppId())
                .and(RpcServer::getHost, rpcServer.getHost())
                .and(RpcServer::getPort, rpcServer.getPort())
                .one();

        if (null == temp) {
            rpcServer.save();
        } else {
            rpcServer.setId(temp.getId());
            rpcServer.update();
        }
    }

    public void saveServices(String appId, Set<String> services) {
        delete().from(RpcService.class).where(RpcService::getAppId, appId).execute();
        for (String service : services) {
            RpcService rpcService = new RpcService();
            rpcService.setAppId(appId);
            rpcService.setServiceId(service);
            rpcService.save();
        }
    }

    public ServerMap getServerMap() {
        List<String> appIdList = select().from(RpcServer.class).map(RpcServer::getAppId).distinct().collect(Collectors.toList());
        ServerMap    serverMap = new ServerMap();
        serverMap.setName("服务列表");
        List<ServerMap> serverMaps = new ArrayList<>(appIdList.size());

        for (String appId : appIdList) {
            ServerMap server = new ServerMap();
            server.setName(appId);
            // TODO search children
            serverMaps.add(server);
        }
        serverMap.setChildren(serverMaps);
        return serverMap;
    }

    public List<ServerVO> getServerList() {
        return select().from(RpcServer.class).map(this::parseServerVO).collect(Collectors.toList());
    }

    private ServerVO parseServerVO(RpcServer rpcServer) {
        ServerVO serverVO = new ServerVO();
        if (StringKit.isNotEmpty(rpcServer.getAppAlias())) {
            serverVO.setName(rpcServer.getAppAlias());
        } else {
            serverVO.setName(rpcServer.getAppId());
        }
        serverVO.setId(rpcServer.getId());
        serverVO.setPid(rpcServer.getPid());
        serverVO.setAddress(rpcServer.getHost() + ":" + rpcServer.getPort());
        serverVO.setOwner(rpcServer.getOwner());
        serverVO.setStatus(rpcServer.getStatus());
        serverVO.setOnlineTime(rpcServer.getOnlineTime());
        return serverVO;
    }

    public ServerDetailVO getServerDetail(Long id) {
        ServerDetailVO serverDetailVO = new ServerDetailVO();

        RpcServer rpcServer = select().from(RpcServer.class).byId(id);

        serverDetailVO.setId(id);
        serverDetailVO.setOwner(rpcServer.getOwner());

        if (StringKit.isNotEmpty(rpcServer.getAppAlias())) {
            serverDetailVO.setName(rpcServer.getAppAlias());
        } else {
            serverDetailVO.setName(rpcServer.getAppId());
        }

        List<RpcServer> nodes = select().from(RpcServer.class).where(RpcServer::getAppId, rpcServer.getAppId()).all();
        serverDetailVO.setNodes(nodes);

        Set<String> services = select().from(RpcService.class).where(RpcService::getAppId, rpcServer.getAppId())
                .map(RpcService::getServiceId).collect(Collectors.toSet());

        serverDetailVO.setServices(services);
        return serverDetailVO;
    }

    public void updateStatus(String host, Integer port, NodeStatusEnum nodeStatus) {
        String status = nodeStatus.toString();
        long count = select().from(RpcServer.class).where(RpcServer::getHost, host)
                .and(RpcServer::getPort, port)
                .and(RpcServer::getStatus, status)
                .count();

        if (count == 0) {
            update().from(RpcServer.class).set(RpcServer::getStatus, status)
                    .where(RpcServer::getHost, host)
                    .and(RpcServer::getPort, Integer.valueOf(port))
                    .execute();
        }

    }
}