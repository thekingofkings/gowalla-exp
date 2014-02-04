% given a pair of user
uaid = 19404; % 819;
ubid = 267; % 956;

ra = importdata(['../../../dataset/sorteddata/', num2str(uaid)]);
rb = importdata(['../../../dataset/sorteddata/', num2str(ubid)]);
latia = zeros(length(ra), 1);
longia = zeros(length(ra), 1);

latib = zeros(length(rb), 1);
longib = zeros(length(rb), 1);


for i = 1:length(ra)
    t = strsplit(ra{i}, ' ');
    latia(i) = str2double(t{3});
    longia(i) = str2double(t{4});
end


for i = 1:length(rb)
    t = strsplit(rb{i}, ' ');
    latib(i) = str2double(t{3});
    longib(i) = str2double(t{4});
end

% DataDensityPlot(longia, latia, 20);
% DataDensityPlot(longib, latib, 20);

coora = [longia, latia];
min_vec = repmat(min(coora), size(coora, 1), 1);
coora = ceil((coora - min_vec)/0.3) + 1;
numx = ceil((max(longia) - min(longia))/0.3)+2;
numy = ceil((max(latia) - min(latia)) / 0.3)+2;
mapa = zeros(numx, numy);
for i = 1:size(coora,1)
    mapa(coora(i, 1), coora(i,2)) = mapa(coora(i,1), coora(i,2)) + 1;
end


coorb = [longib, latib];
min_vec = repmat(min(coorb), size(coorb, 1), 1);
coorb = ceil((coorb - min_vec)/0.3) + 1;
numx = ceil((max(longib) - min(longib))/0.3)+2;
numy = ceil((max(latib) - min(latib)) / 0.3)+2;
mapb = zeros(numx, numy);
for i = 1:size(coorb,1)
    mapb(coorb(i,1), coorb(i,2)) = mapb(coorb(i,1), coorb(i,2)) + 1;
end


mapa = log(mapa + 1);
HeatMap(mapa);
title(num2str(uaid));


mapb = log(mapb + 1);
HeatMap(mapb);
title(num2str(uaid));
