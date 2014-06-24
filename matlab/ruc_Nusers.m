tau = 1;

if tau == 1
    % use 1 hour results
    files = ls('../data_sample_user/stu-u*.txt');
elseif tau == 4
    % use 4 hour results
    files = ls('../data_sample_user/distance-d30-u*-c0.200 (2).txt');
end




numUser = zeros(size(files,1), 1);
for i = 1:size(files,1)
    tmp = textscan(files(i,:), 'stu-u%f.txt');
    numUser(i) = tmp{1};
end

[~, ind] = sort(numUser, 'ascend');
files = files(ind, :);

auc = zeros(size(files,1), 7);

    
figure();
hold on;
for i = 1:size(files,1)
    data = importdata(['../data_sample_user/', files(i,:)]);
    data = data(data(:,6)>0, :);
    
    pbg_locen = data(:,3);
    locen = data(:,4);
    pbg = data(:,5);
    freq = data(:,6);
    pbg_locen_td = data(:,7);
    td = data(:,8);
    friflag = data(:,9);
    
    auc(i,1) = sum(friflag) / length(friflag);

    [~, ~, ~, prec, rec] = precisionRecallPlot( freq, friflag, 'b-');
    auc(i,2) = trapz(rec, prec);
    [~, ~, ~, prec, rec] = precisionRecallPlot( pbg, friflag, 'r--' );
    auc(i,3) = trapz(rec, prec);
    [~, ~, ~, prec, rec] = precisionRecallPlot( locen, friflag, 'g--');
    auc(i,4) = trapz(rec, prec);
    [~, ~, ~, prec, rec] = precisionRecallPlot( td, friflag, 'y--');
    auc(i,5) = trapz(rec, prec);
    [~, ~, ~, prec, rec] = precisionRecallPlot( pbg_locen, friflag, 'c-' );
    auc(i,6) = trapz(rec, prec);
    [~, ~, ~, prec, rec] = precisionRecallPlot( pbg_locen_td, friflag, 'm-.');
    auc(i,7) = trapz(rec, prec);
    
end


f = figure;
bar(auc, 1);
xlabel('# Users', 'fontsize', 20);
ylabel('AUC', 'fontsize', 20);
axis([0, 5, 0, 1.3]);
set(gca, 'linewidth', 3, 'fontsize', 20, 'xticklabel', {1000,  5000,  50000, '107092(all)'});
legend({'Random Guess', 'Frequency', 'Personal', 'Global', 'Temporal', 'Per+Glo', 'Per+Glo+Temp'}, ...
    'location', 'northeast', 'fontsize', 20);
print('GWruc-nusers.eps', '-dpsc');
system('epstopdf GWruc-nusers.eps');
% saveas(gcf, 'GWruc-nusers.png');