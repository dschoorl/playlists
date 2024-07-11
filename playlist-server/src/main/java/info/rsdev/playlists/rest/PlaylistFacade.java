package info.rsdev.playlists.rest;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import info.rsdev.playlists.services.MusicChart;

@RestController
public class PlaylistFacade {
    
    @GetMapping(path = "/api/public/info")
    public InfoObject getInfo() {
        List<String> chartNames = Stream.of(MusicChart.values()).map(chart -> chart.chartName()).toList();
        return new InfoObject(chartNames);
    }

}
