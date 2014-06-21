files = ls('../data_sample_user/distance-*');

numUser = zeros(size(files,1), 1);
for i = 1:size(files,1)
    tmp = textscan(files(i,:), 'distance-d30-u%d-c0.200.txt');
    numUser(i) = tmp{1};
end

[~, ind] = sort(numUser, 'ascend');
files = files(ind, :);

auc = zeros(size(files,1), 7);

    
figure();
hold on;
for i = 1:size(files,1)
    data = importdata(['../data_sample_user/', files(i,:)]);
    
    pbg_locen = data(:,3);
    locen = data(:,4);
    pbg = data(:,5);
    freq = data(:,6);
    pbg_locen_td = data(:,7);
    td = data(:,8);
    friflag = data(:,9);
    
    auc(i,1) = sum(friflag) / length(friflag);

    [prec, rec] = precisionRecallPlot( freq, friflag, 'b-');
    auc(i,2) = trapz(rec, prec);
    [prec, rec] = precisionRecallPlot( pbg, friflag, 'r--' );
    auc(i,3) = trapz(rec, prec);
    [prec, rec] = precisionRecallPlot( locen, friflag, 'g--');
    auc(i,4) = trapz(rec, prec);
    [prec, rec] = precisionRecallPlot( td, friflag, 'y--');
    auc(i,5) = trapz(rec, prec);
    [prec, rec] = precisionRecallPlot( pbg_locen, friflag, 'c-' );
    auc(i,6) = trapz(rec, prec);
    [prec, rec] = precisionRecallPlot( pbg_locen_td, friflag, 'm-.');
    auc(i,7) = trapz(rec, prec);
    
end


f = figure;
bar(auc, 1);
xlabel('# Users', 'fontsize', 20);
ylabel('AUC', 'fontsize', 20);
axis([0, 5, 0, 0.8]);
set(gca, 'linewidth', 2, 'fontsize', 18, 'xticklabel', {1000,  5000,  50000, '107092(all)'});
legend({'Random Guess', 'Frequency', 'Personal', 'Global', 'Temp Depen', 'Per+Glo', 'Per+Glo+Tem'}, ...
    'location', 'northeast', 'fontsize', 16);
print('GWruc-nusers.eps', '-dpsc');
system('epstopdf GWruc-nusers.eps');